# 아키텍처 의사결정 기록 (Architecture Decision Records)

이 문서는 보일러플레이트의 주요 아키텍처 결정 사항과 그 이유를 설명합니다.

---

## ADR-001: DTO 명명 규칙

**일자**: 2025-01-19
**상태**: ✅ Accepted
**결정자**: Development Team

### 컨텍스트

DTO 클래스의 명명 규칙이 일관되지 않아 개발자들이 혼란스러워합니다:
- 일부는 `RequestSample`, `ResponseSample` (Request/Response가 앞)
- 대부분은 `LoginRequest`, `TokenResponse` (Request/Response가 뒤)

### 결정

**도메인 이름을 앞에, Request/Response를 뒤에 배치**

```java
// ✅ 올바른 패턴
SampleRequest.java
SampleResponse.java
LoginRequest.java
TokenResponse.java

// ❌ 잘못된 패턴
RequestSample.java
ResponseSample.java
```

### 이유

1. **알파벳순 그룹화**: 관련 클래스들이 파일 탐색기에서 함께 나타남
   ```
   Sample.java
   SampleRequest.java
   SampleResponse.java
   SampleMapper.java
   ```

2. **Spring Boot 표준**: Spring 공식 가이드와 대부분의 튜토리얼에서 사용하는 패턴

3. **가독성**: 도메인 개념이 먼저 눈에 들어와 이해하기 쉬움

4. **확장성**: `CreateSampleRequest`, `UpdateSampleRequest` 등으로 확장 가능

### 영향

- 기존 `RequestSample`, `ResponseSample` 파일명 변경 필요
- 모든 import 문 업데이트 필요
- MapStruct 매퍼 업데이트 필요

---

## ADR-002: Constructor Injection 강제

**일자**: 2024-XX-XX
**상태**: ✅ Accepted

### 컨텍스트

Spring에서는 세 가지 의존성 주입 방법을 지원합니다:
- Field Injection (`@Autowired private SomeService service;`)
- Setter Injection
- Constructor Injection

### 결정

**모든 컴포넌트에서 Constructor Injection만 사용**

```java
// ✅ 올바른 방법
@RequiredArgsConstructor
@Service
public class SampleService {
    private final SampleRepository repository;
    private final SampleMapper mapper;
}

// ❌ 금지된 방법
@Service
public class SampleService {
    @Autowired
    private SampleRepository repository;  // Field Injection 금지
}
```

### 이유

1. **불변성**: `final` 키워드로 필드를 불변으로 만들 수 있음
2. **테스트 용이성**: Mock 객체 주입이 쉬움
3. **명시적 의존성**: 클래스가 어떤 의존성을 가지는지 명확
4. **순환 의존성 방지**: 컴파일 타임에 순환 의존성 감지
5. **Spring 권장사항**: Spring 팀이 공식 권장하는 방법

### 구현

- Lombok의 `@RequiredArgsConstructor` 사용
- `final` 필드로 선언하여 불변성 보장

### 예외

없음. 모든 경우에 Constructor Injection 사용.

---

## ADR-003: Transactional Outbox Pattern

**일자**: 2024-XX-XX
**상태**: ✅ Accepted

### 컨텍스트

마이크로서비스 아키텍처에서 데이터베이스 트랜잭션과 Kafka 이벤트 발행을 원자적으로 처리해야 합니다.

**문제점**:
```java
// ❌ 문제: DB 커밋 성공 후 Kafka 발행 실패 시 데이터 불일치
@Transactional
public void createUser(User user) {
    repository.save(user);  // ✅ DB 저장 성공
    kafkaTemplate.send("user-created", event);  // ❌ Kafka 실패
    // → DB에는 저장되었으나 이벤트는 발행되지 않음
}
```

### 결정

**Transactional Outbox 패턴 사용**

```java
// ✅ 해결책: 이벤트를 DB에 먼저 저장
@Transactional
public void createUser(User user) {
    repository.save(user);  // 1. User 저장
    outboxEventPublisher.publish(topic, event);  // 2. Outbox 테이블에 이벤트 저장
    // → 동일한 트랜잭션, 원자성 보장
}

// 별도 스케줄러가 Outbox 테이블을 폴링하여 Kafka로 발행
@Scheduled(fixedDelay = 5000)
public void publishPendingEvents() {
    List<OutboxEvent> events = outboxRepository.findByStatus(PENDING);
    events.forEach(event -> {
        kafkaTemplate.send(event.getTopic(), event.getPayload());
        event.setStatus(PUBLISHED);
    });
}
```

### 이유

1. **데이터 일관성**: DB와 이벤트 발행의 원자성 보장
2. **신뢰성**: Kafka 장애 시에도 이벤트 유실 없음
3. **재시도 메커니즘**: 발행 실패 시 자동 재시도
4. **감사 추적**: 모든 이벤트가 DB에 기록됨

### 구현 상세

**엔티티**:
```java
@Entity
public class OutboxEvent {
    @Id
    private String eventId;
    private String topic;
    private String payload;  // JSON
    private OutboxEventStatus status;  // PENDING, PUBLISHED, FAILED
    private LocalDateTime createdAt;
    private int retryCount;
}
```

**Publisher**:
```java
@Transactional(propagation = Propagation.MANDATORY)
public void publish(String topic, DomainEvent event) {
    // 반드시 기존 트랜잭션 내에서만 호출
    outboxEventRepository.save(new OutboxEvent(topic, event));
}
```

**Scheduler**:
```java
@Scheduled(fixedDelay = 5000)
public void publishEvents() {
    // PENDING 상태 이벤트 조회 및 Kafka 발행
}
```

### 트레이드오프

**장점**:
- ✅ 데이터 일관성 보장
- ✅ 이벤트 유실 방지
- ✅ 재시도 메커니즘

**단점**:
- ⚠️ 약간의 지연 (최대 5초)
- ⚠️ Outbox 테이블 관리 필요
- ⚠️ 정기적인 cleanup 필요 (오래된 PUBLISHED 이벤트 삭제)

---

## ADR-004: Event Publishing 전략

**일자**: 2024-XX-XX
**상태**: ✅ Accepted

### 컨텍스트

애플리케이션에 3가지 이벤트 발행 방법이 존재합니다:
1. `ApplicationEventPublisher` - Spring 내부 이벤트
2. `EventPublisher` - 직접 Kafka 발행
3. `OutboxEventPublisher` - Transactional Outbox 패턴

### 결정

| 상황 | 사용할 Publisher | 이유 |
|------|------------------|------|
| **같은 JVM 내 처리** | `ApplicationEventPublisher` | 빠르고 간단, 트랜잭션 전파 |
| **다른 서비스 통신 (중요)** | `OutboxEventPublisher` | 데이터 일관성 보장 |
| **다른 서비스 통신 (비중요)** | `EventPublisher` | 빠른 처리, 유실 허용 |

### 예시

**Case 1: 같은 서비스 내 처리**
```java
// ✅ ApplicationEventPublisher 사용
@Transactional
public void createSample(SampleRequest dto) {
    Sample entity = repository.save(sample);

    // 같은 JVM 내에서 캐시 무효화, 통계 업데이트 등
    publisher.publishEvent(new SampleCreatedEvent(entity.getId()));
}

@EventListener
public void handleSampleCreated(SampleCreatedEvent event) {
    cacheService.invalidate("sample-" + event.getId());
}
```

**Case 2: 다른 서비스 통신 (중요한 이벤트)**
```java
// ✅ OutboxEventPublisher 사용
@Transactional
public void createOrder(Order order) {
    repository.save(order);

    // 결제 서비스로 이벤트 발행 - 유실되면 안 됨!
    outboxEventPublisher.publish("payment.events",
        new OrderCreatedEvent(order));
}
```

**Case 3: 다른 서비스 통신 (비중요한 이벤트)**
```java
// ✅ EventPublisher 사용 (빠른 처리, 유실 허용 가능)
public void logUserActivity(String userId, String action) {
    // 로그 수집 서비스로 발행 - 유실되어도 큰 문제 없음
    eventPublisher.publish("analytics.events",
        new UserActivityEvent(userId, action));
}
```

### 의사결정 플로우차트

```
이벤트 발행 필요?
    │
    ├─ 같은 JVM 내 처리?
    │   └─→ ApplicationEventPublisher
    │
    └─ 다른 서비스 통신?
        │
        ├─ 이벤트 유실되면 안 되나?
        │   ├─ Yes → OutboxEventPublisher
        │   └─ No → EventPublisher
```

---

## ADR-005: Soft Delete 전략

**일자**: 2024-XX-XX
**상태**: ✅ Accepted

### 컨텍스트

사용자 데이터 삭제 시 물리적 삭제 vs 논리적 삭제 선택.

### 결정

**Soft Delete (논리적 삭제) 사용**

```java
@MappedSuperclass
public abstract class BaseEntity {
    private Boolean deleted = false;
    private LocalDateTime deletedAt;

    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
```

### 이유

1. **데이터 복구 가능**: 실수로 삭제한 데이터 복구 가능
2. **감사 추적**: 삭제된 데이터의 히스토리 유지
3. **GDPR 준수**: 삭제 요청 후에도 일정 기간 데이터 보관 가능
4. **외래 키 무결성**: 관계 데이터 유지
5. **분석 가능**: 삭제된 데이터 패턴 분석

### 구현

**Repository 쿼리**:
```java
public interface SampleRepository extends JpaRepository<Sample, Long> {
    Optional<Sample> findBySampleIdAndDeletedFalse(String sampleId);
    List<Sample> findByDeletedFalse();
}
```

**Service**:
```java
@Transactional
public void deleteSample(String sampleId) {
    Sample sample = repository.findBySampleIdAndDeletedFalse(sampleId)
        .orElseThrow(...);
    sample.delete();  // soft delete
    repository.save(sample);
}
```

### 주의사항

1. **UNIQUE 제약조건**: `deleted=false`인 행에만 적용
   ```sql
   CREATE UNIQUE INDEX idx_sample_id_active
   ON sample(sample_id) WHERE deleted = false;
   ```

2. **정기적인 Cleanup**: 오래된 deleted 데이터 정리
   ```java
   @Scheduled(cron = "0 0 2 * * *")  // 매일 새벽 2시
   public void cleanupOldDeletedData() {
       LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
       repository.hardDeleteBeforeDate(cutoff);
   }
   ```

---

## ADR-006: 패키지 구조

**일자**: 2024-XX-XX
**상태**: ✅ Accepted

### 컨텍스트

코드를 어떻게 조직화할 것인가?

### 결정

**기능별(Feature) + 레이어별(Layer) 하이브리드 구조**

```
com.hamkkebu.boilerplate/
├── common/                    # 공통 기능 (횡단 관심사)
│   ├── config/                # 설정
│   ├── security/              # 보안
│   ├── exception/             # 예외 처리
│   ├── audit/                 # 감사
│   ├── dto/                   # 공통 DTO
│   └── util/                  # 유틸리티
│
├── controller/                # 레이어: REST 컨트롤러
├── service/                   # 레이어: 비즈니스 로직
├── repository/                # 레이어: 데이터 접근
│
├── data/                      # 도메인 데이터
│   ├── entity/                # JPA 엔티티
│   ├── dto/                   # 도메인 DTO
│   ├── mapper/                # DTO-Entity 변환
│   └── event/                 # 도메인 이벤트
│
├── listener/                  # 이벤트 리스너
├── publisher/                 # 이벤트 발행자
└── scheduler/                 # 스케줄 작업
```

### 이유

1. **횡단 관심사 분리**: `common/` 패키지로 재사용 가능한 코드 격리
2. **레이어 명확성**: Controller, Service, Repository 레이어 명확
3. **도메인 응집도**: 관련 데이터 클래스를 `data/` 패키지에 그룹화
4. **확장 용이성**: 새로운 도메인 추가 시 `data/entity`에 추가만 하면 됨

### Configuration 파일 위치 규칙

| 타입 | 위치 | 예시 |
|------|------|------|
| **인프라 설정** | `/common/config/` | SecurityConfig, WebMvcConfig, JpaConfig |
| **비즈니스 설정** | `/config/` | KafkaTopicConfig, DomainRuleConfig |

---

## ADR-007: Exception Handling 전략

**일자**: 2024-XX-XX
**상태**: ✅ Accepted

### 컨텍스트

예외를 어떻게 처리하고 클라이언트에게 전달할 것인가?

### 결정

**GlobalExceptionHandler + BusinessException + ErrorCode enum**

```java
// 1. ErrorCode enum으로 표준화
public enum ErrorCode {
    USER_NOT_FOUND("USER-101", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    AUTHENTICATION_FAILED("AUTH-401", "인증에 실패했습니다", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

// 2. BusinessException으로 감싸기
throw new BusinessException(ErrorCode.USER_NOT_FOUND);

// 3. GlobalExceptionHandler로 잡기
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handle(BusinessException ex) {
        return ResponseEntity
            .status(ex.getErrorCode().getStatus())
            .body(ApiResponse.error(ex.getErrorCode()));
    }
}
```

### 응답 형식

```json
{
  "success": false,
  "error": {
    "code": "USER-101",
    "message": "사용자를 찾을 수 없습니다",
    "details": {}
  },
  "timestamp": "2025-01-19T10:00:00"
}
```

### 이유

1. **일관성**: 모든 API가 동일한 에러 응답 형식
2. **문서화**: ErrorCode enum으로 모든 에러 코드 한 곳에서 관리
3. **국제화**: 메시지를 쉽게 다국어로 변경 가능
4. **클라이언트 친화적**: 에러 코드로 클라이언트가 처리 가능

---

## ADR-008: DTO vs Entity 분리

**일자**: 2024-XX-XX
**상태**: ✅ Accepted

### 결정

**Entity를 절대 Controller에 직접 노출하지 않음**

```java
// ❌ 잘못된 방법
@PostMapping
public Sample createSample(@RequestBody Sample sample) {  // Entity 직접 노출
    return repository.save(sample);
}

// ✅ 올바른 방법
@PostMapping
public ApiResponse<SampleResponse> createSample(@RequestBody SampleRequest dto) {
    Sample entity = mapper.toEntity(dto);
    entity = repository.save(entity);
    return ApiResponse.success(mapper.toDto(entity));
}
```

### 이유

1. **보안**: Entity의 민감한 필드(password 등) 노출 방지
2. **캡슐화**: 데이터베이스 스키마 변경이 API에 영향 없음
3. **검증**: DTO에만 Validation 적용
4. **버전 관리**: API 버전별로 다른 DTO 사용 가능

### 변환 전략

**MapStruct 사용**:
```java
@Mapper(componentModel = "spring")
public interface SampleMapper {
    Sample toEntity(SampleRequest dto);
    SampleResponse toDto(Sample entity);
}
```

---

## ADR-009: API Response 표준화

**일자**: 2024-XX-XX
**상태**: ✅ Accepted

### 결정

**모든 API는 ApiResponse<T> 래퍼 사용**

```java
@Getter
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorResponse error;
    private String message;
    private String timestamp;
}
```

### 예시

**성공 응답**:
```json
{
  "success": true,
  "data": {
    "sampleId": "john",
    "sampleEmail": "john@example.com"
  },
  "message": "Success",
  "timestamp": "2025-01-19T10:00:00"
}
```

**실패 응답**:
```json
{
  "success": false,
  "error": {
    "code": "USER-101",
    "message": "사용자를 찾을 수 없습니다"
  },
  "timestamp": "2025-01-19T10:00:00"
}
```

### 이유

1. **클라이언트 편의성**: `success` 필드로 성공/실패 즉시 판별
2. **일관성**: 모든 API가 동일한 구조
3. **타입 안전성**: 제네릭으로 타입 안전성 보장
4. **메타데이터**: timestamp 등 공통 정보 포함

---

## 요약

| ADR | 결정 사항 | 핵심 이유 |
|-----|-----------|-----------|
| ADR-001 | DTO 명명: `도메인 + Request/Response` | 가독성, 그룹화, Spring 표준 |
| ADR-002 | Constructor Injection 강제 | 불변성, 테스트 용이성, 명시적 의존성 |
| ADR-003 | Transactional Outbox Pattern | 데이터 일관성, 이벤트 유실 방지 |
| ADR-004 | Event Publishing 전략 | 상황별 최적 Publisher 선택 |
| ADR-005 | Soft Delete | 데이터 복구, 감사 추적, GDPR |
| ADR-006 | 패키지 구조 | 횡단 관심사 분리, 레이어 명확성 |
| ADR-007 | Exception Handling | 일관성, 문서화, 클라이언트 친화성 |
| ADR-008 | DTO vs Entity 분리 | 보안, 캡슐화, 검증 분리 |
| ADR-009 | API Response 표준화 | 일관성, 타입 안전성, 메타데이터 |

---

**작성일**: 2025-01-19
**최종 업데이트**: 2025-01-19
**버전**: 1.0