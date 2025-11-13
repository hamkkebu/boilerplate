# Kafka Zero-Payload 이벤트 시스템 가이드

## 📋 목차
1. [개요](#개요)
2. [Zero-Payload 패턴이란?](#zero-payload-패턴이란)
3. [구조](#구조)
4. [설정](#설정)
5. [이벤트 발행 (Producer)](#이벤트-발행-producer)
6. [이벤트 구독 (Consumer)](#이벤트-구독-consumer)
7. [실전 예제](#실전-예제)
8. [모범 사례](#모범-사례)
9. [트러블슈팅](#트러블슈팅)

---

## 개요

이 프로젝트는 **Zero-Payload 패턴**을 따르는 Kafka 이벤트 시스템을 제공합니다.

### 주요 특징
- ✅ **Zero-Payload 패턴**: 이벤트에 ID만 포함, 데이터는 API로 조회
- ✅ **이벤트 크기 최소화**: 네트워크 효율성 극대화
- ✅ **데이터 일관성 보장**: 항상 최신 데이터 조회
- ✅ **보안 강화**: 민감한 정보 노출 방지
- ✅ **타입 안전성**: Java 타입 시스템 활용

---

## Zero-Payload 패턴이란?

### 기존 방식 (Full Payload)
```json
{
  "eventId": "evt-123",
  "eventType": "USER_CREATED",
  "user": {
    "id": "user-123",
    "name": "홍길동",
    "email": "hong@example.com",
    "phoneNumber": "010-1234-5678",
    "address": "서울시 강남구...",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```
**문제점:**
- ❌ 이벤트 크기가 큼 (네트워크 비용 증가)
- ❌ 민감한 정보 노출 위험
- ❌ 데이터 불일치 가능성 (이벤트 발행 후 데이터 변경)
- ❌ 스키마 변경 시 모든 Consumer 수정 필요

### Zero-Payload 방식
```json
{
  "eventId": "evt-123",
  "eventType": "USER_CREATED",
  "resourceId": "user-123",
  "userId": "user-123",
  "occurredAt": "2024-01-01T10:00:00"
}
```
**장점:**
- ✅ 이벤트 크기 최소화 (90% 이상 감소)
- ✅ 민감한 정보 노출 방지
- ✅ 데이터 일관성 보장 (Consumer가 항상 최신 데이터 조회)
- ✅ 스키마 변경에 유연
- ✅ 보안 향상 (권한 체크를 API에서 수행)

---

## 구조

### 디렉토리 구조
```
backend/
├── config/
│   ├── KafkaConfig.java              # Kafka Producer/Consumer 설정
│   └── KafkaEventFilterConfig.java   # 이벤트 필터 설정
│
├── data/event/
│   ├── DomainEvent.java              # 이벤트 인터페이스
│   ├── BaseEvent.java                # 기본 이벤트 클래스
│   ├── UserCreatedEvent.java
│   ├── LedgerCreatedEvent.java
│   ├── TransactionCreatedEvent.java
│   ├── TransactionUpdatedEvent.java
│   └── TransactionDeletedEvent.java
│
├── publisher/
│   └── EventPublisher.java           # 이벤트 발행
│
└── listener/
    ├── UserEventListener.java        # 사용자 이벤트 리스너
    └── TransactionEventListener.java # 거래 이벤트 리스너
```

### 클래스 다이어그램
```
DomainEvent (interface)
    ↑
BaseEvent (abstract)
    ↑
    ├── UserCreatedEvent
    ├── LedgerCreatedEvent
    └── TransactionCreatedEvent
```

---

## 설정

### 1. application.yml 설정

`application.yml`에 Kafka 설정 추가:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3

    consumer:
      group-id: your-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest

      properties:
        spring.json.trusted.packages: "*"
```

### 2. Kafka 실행 (Docker)

```bash
# docker-compose.yml
version: '3'
services:
  kafka:
    image: confluentinc/cp-kafka:latest
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
```

```bash
docker-compose up -d
```

---

## 이벤트 발행 (Producer)

### 1. 기본 사용법

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final EventPublisher eventPublisher;

    public User createUser(UserCreateRequest request) {
        // 1. 사용자 생성
        User user = userRepository.save(new User(request));

        // 2. 이벤트 발행
        UserCreatedEvent event = UserCreatedEvent.builder()
            .userId(user.getId())
            .build();

        eventPublisher.publish("user.events", event);

        return user;
    }
}
```

### 2. Builder 패턴

```java
TransactionCreatedEvent event = TransactionCreatedEvent.builder()
    .transactionId("tx-123")
    .userId("user-123")
    .ledgerId("ledger-123")
    .metadata("{\"amount\": 5000, \"category\": \"식비\"}")
    .build();

eventPublisher.publish("transaction.events", event);
```

### 3. 간단한 생성자

```java
LedgerCreatedEvent event = new LedgerCreatedEvent("ledger-123", "user-123");
eventPublisher.publish("ledger.events", event);
```

### 4. 동기 발행 (발행 완료 대기)

```java
// 중요한 이벤트는 동기 발행 (발행 실패 시 예외 발생)
eventPublisher.publishSync("transaction.events", event);
```

### 5. 배치 발행

```java
List<TransactionCreatedEvent> events = transactions.stream()
    .map(tx -> new TransactionCreatedEvent(tx.getId(), tx.getUserId(), tx.getLedgerId()))
    .toList();

eventPublisher.publishBatch("transaction.events", events);
```

---

## 이벤트 구독 (Consumer)

### 1. 기본 리스너

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final UserServiceClient userServiceClient;

    @KafkaListener(
        topics = "user.events",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent: userId={}", event.getUserId());

        // Zero-Payload 패턴: API를 호출하여 상세 정보 조회
        User user = userServiceClient.getUser(event.getUserId());

        // 비즈니스 로직 수행
        sendWelcomeEmail(user);
    }
}
```

### 2. 이벤트 타입별 처리

```java
@KafkaListener(topics = "transaction.events", groupId = "analytics-service")
public void handleTransactionEvents(BaseEvent event) {
    switch (event.getEventType()) {
        case "TRANSACTION_CREATED":
            handleCreated((TransactionCreatedEvent) event);
            break;
        case "TRANSACTION_UPDATED":
            handleUpdated((TransactionUpdatedEvent) event);
            break;
        case "TRANSACTION_DELETED":
            handleDeleted((TransactionDeletedEvent) event);
            break;
    }
}
```

### 3. 에러 처리

```java
@KafkaListener(topics = "transaction.events")
public void handleEvent(TransactionCreatedEvent event) {
    try {
        processEvent(event);
    } catch (Exception ex) {
        log.error("Failed to process event: eventId={}, error={}",
            event.getEventId(), ex.getMessage(), ex);

        // DLQ로 전송하거나 재시도 로직 추가
        sendToDLQ(event, ex);
    }
}
```

---

## 실전 예제

### 예제 1: 거래 생성 → 통계 업데이트

**Transaction Service (Producer)**
```java
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final EventPublisher eventPublisher;

    public Transaction createTransaction(TransactionCreateRequest request) {
        // 1. 거래 저장
        Transaction transaction = transactionRepository.save(
            new Transaction(request)
        );

        // 2. 이벤트 발행
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
            .transactionId(transaction.getId())
            .userId(transaction.getUserId())
            .ledgerId(transaction.getLedgerId())
            .build();

        eventPublisher.publish("transaction.events", event);

        return transaction;
    }
}
```

**Analytics Service (Consumer)**
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventListener {

    private final TransactionServiceClient transactionClient;
    private final StatisticsService statisticsService;

    @KafkaListener(topics = "transaction.events", groupId = "analytics-service")
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("Updating statistics for transaction: {}", event.getResourceId());

        // 1. Transaction API를 호출하여 상세 정보 조회
        Transaction transaction = transactionClient.getTransaction(
            event.getResourceId()
        );

        // 2. 통계 업데이트
        statisticsService.updateMonthlyStats(
            transaction.getLedgerId(),
            transaction.getAmount(),
            transaction.getCategory()
        );

        log.info("Statistics updated successfully");
    }
}
```

### 예제 2: 사용자 생성 → 환영 이메일 발송

**Auth Service (Producer)**
```java
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public User register(RegisterRequest request) {
        User user = userRepository.save(new User(request));

        // 환영 이메일 발송을 위한 이벤트 발행
        UserCreatedEvent event = new UserCreatedEvent(user.getId());
        eventPublisher.publish("user.events", event);

        return user;
    }
}
```

**Notification Service (Consumer)**
```java
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final UserServiceClient userClient;
    private final EmailService emailService;

    @KafkaListener(topics = "user.events", groupId = "notification-service")
    public void handleUserCreated(UserCreatedEvent event) {
        // 사용자 정보 조회
        User user = userClient.getUser(event.getUserId());

        // 환영 이메일 발송
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());
    }
}
```

---

## 모범 사례

### 1. 이벤트 명명 규칙
```
{RESOURCE}_{ACTION}
예: USER_CREATED, TRANSACTION_UPDATED, LEDGER_DELETED
```

### 2. 토픽 구조
```
{domain}.events
예: user.events, transaction.events, ledger.events
```

### 3. Group ID 명명
```
{service-name}-group
예: analytics-service-group, notification-service-group
```

### 4. 에러 처리
```java
@KafkaListener(topics = "transaction.events")
public void handleEvent(TransactionCreatedEvent event) {
    try {
        processEvent(event);
    } catch (RecoverableException ex) {
        // 재시도 가능한 에러: 재시도
        throw ex;
    } catch (Exception ex) {
        // 재시도 불가능한 에러: DLQ로 전송
        log.error("Unrecoverable error", ex);
        // DLQ 처리 로직
    }
}
```

### 5. 멱등성 보장
```java
@KafkaListener(topics = "user.events")
public void handleUserCreated(UserCreatedEvent event) {
    // 이미 처리된 이벤트인지 확인
    if (processedEventRepository.exists(event.getEventId())) {
        log.info("Event already processed: {}", event.getEventId());
        return;
    }

    processEvent(event);

    // 처리 완료 기록
    processedEventRepository.save(event.getEventId());
}
```

---

## 트러블슈팅

### 1. Kafka 연결 실패
```
Error: Connection to node -1 could not be established
```
**해결:**
```bash
# Kafka 실행 확인
docker ps | grep kafka

# 포트 확인
netstat -an | grep 9092
```

### 2. Deserialization 에러
```
Error: Cannot deserialize value
```
**해결:**
```yaml
spring:
  kafka:
    consumer:
      properties:
        spring.json.trusted.packages: "*"
```

### 3. Consumer가 이벤트를 받지 못함
**확인 사항:**
- Group ID가 올바른지 확인
- 토픽이 올바른지 확인
- Offset이 초기화되지 않았는지 확인

```bash
# Offset 초기화
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --group your-group-id --reset-offsets --to-earliest --topic your-topic --execute
```

### 4. 메시지 순서 보장
Kafka는 **파티션 내에서만** 순서를 보장합니다.

```java
// 같은 리소스의 이벤트는 같은 파티션으로 전송 (Key 사용)
eventPublisher.publish("transaction.events", event);  // resourceId가 Key로 사용됨
```

---

## 추가 리소스

- [Kafka 공식 문서](https://kafka.apache.org/documentation/)
- [Spring Kafka 공식 문서](https://docs.spring.io/spring-kafka/reference/)
- [Zero-Payload 패턴 설명](https://microservices.io/patterns/data/event-driven-architecture.html)

---

## 문의

궁금한 점이 있으면 이슈를 등록해주세요!
