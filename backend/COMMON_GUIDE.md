# 공통 기능 가이드

## 📋 목차
1. [개요](#개요)
2. [ApiResponse - 통일된 API 응답](#apiresponse)
3. [ErrorCode - 표준 에러 코드](#errorcode)
4. [BusinessException - 비즈니스 예외](#businessexception)
5. [GlobalExceptionHandler - 전역 예외 처리](#globalexceptionhandler)
6. [DateTimeUtil - 날짜/시간 유틸리티](#datetimeutil)
7. [PageRequest & PageResponse - 페이징](#paging)
8. [CommonConstants - 공통 상수](#commonconstants)
9. [실전 예제](#실전-예제)

---

## 개요

이 프로젝트는 공통적으로 사용되는 기능들을 표준화하여 제공합니다.

### 주요 특징
- ✅ **통일된 API 응답 형식**: 모든 API가 일관된 응답 구조
- ✅ **표준화된 에러 처리**: 명확한 에러 코드와 메시지
- ✅ **자동 예외 처리**: GlobalExceptionHandler가 모든 예외를 자동 처리
- ✅ **풍부한 유틸리티**: 날짜/시간, 페이징 등
- ✅ **타입 안전성**: Enum과 제네릭을 활용한 타입 안전 보장

---

## ApiResponse

### 기본 사용법

#### 성공 응답

```java
@GetMapping("/users/{id}")
public ApiResponse<User> getUser(@PathVariable String id) {
    User user = userService.findById(id);
    return ApiResponse.success(user);
}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "id": "user-123",
    "name": "홍길동",
    "email": "hong@example.com"
  },
  "message": "Success",
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 성공 응답 (커스텀 메시지)

```java
@PostMapping("/users")
public ApiResponse<User> createUser(@RequestBody UserCreateRequest request) {
    User user = userService.create(request);
    return ApiResponse.success(user, "사용자가 성공적으로 생성되었습니다");
}
```

#### 성공 응답 (데이터 없음)

```java
@DeleteMapping("/users/{id}")
public ApiResponse<Void> deleteUser(@PathVariable String id) {
    userService.delete(id);
    return ApiResponse.success("사용자가 삭제되었습니다");
}
```

### 에러 응답

에러는 `BusinessException`을 던지면 자동으로 처리됩니다.

```java
@GetMapping("/users/{id}")
public ApiResponse<User> getUser(@PathVariable String id) {
    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
}
```

**응답:**
```json
{
  "success": false,
  "error": {
    "code": "USER-101",
    "message": "사용자를 찾을 수 없습니다"
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

---

## ErrorCode

### 에러 코드 구조

```
{CATEGORY}-{NUMBER}
```

| Category | Range | 설명 |
|----------|-------|------|
| COMMON | 001-099 | 공통 에러 |
| USER | 101-199 | 사용자 관련 |
| LEDGER | 201-299 | 가계부 관련 |
| TRANSACTION | 301-399 | 거래 관련 |
| AUTH | 401-499 | 인증/인가 관련 |
| KAFKA | 501-599 | Kafka 관련 |

### 주요 에러 코드

```java
// 공통
ErrorCode.INVALID_REQUEST         // "잘못된 요청입니다"
ErrorCode.RESOURCE_NOT_FOUND      // "요청한 리소스를 찾을 수 없습니다"
ErrorCode.VALIDATION_FAILED       // "입력값 검증에 실패했습니다"

// 사용자
ErrorCode.USER_NOT_FOUND          // "사용자를 찾을 수 없습니다"
ErrorCode.EMAIL_ALREADY_EXISTS    // "이미 사용 중인 이메일입니다"
ErrorCode.PASSWORD_MISMATCH       // "비밀번호가 일치하지 않습니다"

// 가계부
ErrorCode.LEDGER_NOT_FOUND        // "가계부를 찾을 수 없습니다"
ErrorCode.LEDGER_ACCESS_DENIED    // "가계부에 접근할 권한이 없습니다"

// 거래
ErrorCode.TRANSACTION_NOT_FOUND   // "거래를 찾을 수 없습니다"
ErrorCode.INVALID_TRANSACTION_AMOUNT // "잘못된 거래 금액입니다"

// 인증
ErrorCode.AUTHENTICATION_FAILED   // "인증에 실패했습니다"
ErrorCode.EXPIRED_TOKEN          // "만료된 인증 토큰입니다"
ErrorCode.ACCESS_DENIED          // "접근 권한이 없습니다"
```

### 새로운 에러 코드 추가하기

`ErrorCode.java` 파일에 추가:

```java
/**
 * 예산 초과
 */
BUDGET_EXCEEDED("TRANSACTION-306", "예산을 초과했습니다", HttpStatus.BAD_REQUEST)
```

---

## BusinessException

### 기본 사용법

#### 1. 기본 에러

```java
if (user == null) {
    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
}
```

#### 2. 커스텀 메시지

```java
throw new BusinessException(
    ErrorCode.USER_NOT_FOUND,
    "사용자를 찾을 수 없습니다: ID=" + userId
);
```

#### 3. 상세 정보 포함

```java
throw new BusinessException(
    ErrorCode.VALIDATION_FAILED,
    "입력값 검증 실패",
    Map.of(
        "field", "email",
        "value", request.getEmail(),
        "reason", "이메일 형식이 올바르지 않습니다"
    )
);
```

**응답:**
```json
{
  "success": false,
  "error": {
    "code": "COMMON-009",
    "message": "입력값 검증 실패",
    "details": {
      "field": "email",
      "value": "invalid-email",
      "reason": "이메일 형식이 올바르지 않습니다"
    }
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 4. 원인 예외 포함

```java
try {
    externalApiCall();
} catch (Exception ex) {
    throw new BusinessException(
        ErrorCode.EXTERNAL_API_ERROR,
        "외부 API 호출 실패",
        ex
    );
}
```

#### 5. 동적으로 상세 정보 추가

```java
throw new BusinessException(ErrorCode.VALIDATION_FAILED)
    .addDetail("field1", "error message 1")
    .addDetail("field2", "error message 2");
```

---

## GlobalExceptionHandler

### 자동 처리되는 예외

GlobalExceptionHandler가 자동으로 처리하는 예외 목록:

| 예외 | 에러 코드 | HTTP 상태 |
|------|----------|----------|
| BusinessException | 예외의 ErrorCode | ErrorCode의 HttpStatus |
| MethodArgumentNotValidException | VALIDATION_FAILED | 400 |
| MissingServletRequestParameterException | MISSING_PARAMETER | 400 |
| HttpRequestMethodNotSupportedException | METHOD_NOT_ALLOWED | 405 |
| IllegalArgumentException | INVALID_REQUEST | 400 |
| Exception (기타) | INTERNAL_SERVER_ERROR | 500 |

### Validation 예외 처리

#### @Valid 사용

```java
@PostMapping("/users")
public ApiResponse<User> createUser(@Valid @RequestBody UserCreateRequest request) {
    // Validation 통과 시 실행
    User user = userService.create(request);
    return ApiResponse.success(user);
}
```

```java
public class UserCreateRequest {

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하여야 합니다")
    private String name;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;
}
```

**Validation 실패 시 응답:**
```json
{
  "success": false,
  "error": {
    "code": "COMMON-009",
    "message": "입력값 검증에 실패했습니다",
    "details": {
      "name": "이름은 필수입니다",
      "email": "이메일 형식이 올바르지 않습니다"
    }
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

---

## DateTimeUtil

### 주요 기능

#### 현재 시각

```java
LocalDateTime now = DateTimeUtil.now();
LocalDate today = DateTimeUtil.today();
LocalTime time = DateTimeUtil.currentTime();
```

#### 포맷팅

```java
// LocalDateTime → String
String formatted = DateTimeUtil.format(now);
// "2024-01-01 10:00:00"

String custom = DateTimeUtil.format(now, "yyyy/MM/dd HH:mm");
// "2024/01/01 10:00"

// LocalDate → String
String date = DateTimeUtil.formatDate(LocalDate.now());
// "2024-01-01"
```

#### 파싱

```java
// String → LocalDateTime
LocalDateTime dateTime = DateTimeUtil.parse("2024-01-01 10:00:00");

LocalDate date = DateTimeUtil.parseDate("2024-01-01");
```

#### 날짜 계산

```java
LocalDateTime now = DateTimeUtil.now();

// 7일 후
LocalDateTime after7Days = DateTimeUtil.plusDays(now, 7);

// 3개월 후
LocalDateTime after3Months = DateTimeUtil.plusMonths(now, 3);

// 1년 전
LocalDateTime before1Year = DateTimeUtil.minusYears(now, -1);

// 2시간 후
LocalDateTime after2Hours = DateTimeUtil.plusHours(now, 2);
```

#### 날짜 차이

```java
LocalDateTime start = DateTimeUtil.parse("2024-01-01 10:00:00");
LocalDateTime end = DateTimeUtil.parse("2024-01-08 10:00:00");

long days = DateTimeUtil.daysBetween(start, end);        // 7
long hours = DateTimeUtil.hoursBetween(start, end);      // 168
long minutes = DateTimeUtil.minutesBetween(start, end);  // 10080
```

#### 날짜 비교

```java
LocalDateTime date1 = DateTimeUtil.parse("2024-01-01 10:00:00");
LocalDateTime date2 = DateTimeUtil.parse("2024-01-02 10:00:00");

boolean isBefore = DateTimeUtil.isBefore(date1, date2);  // true
boolean isAfter = DateTimeUtil.isAfter(date1, date2);    // false
boolean isEqual = DateTimeUtil.isEqual(date1, date1);    // true

// 범위 체크
boolean inRange = DateTimeUtil.isBetween(
    DateTimeUtil.now(),
    DateTimeUtil.startOfMonth(DateTimeUtil.now()),
    DateTimeUtil.endOfMonth(DateTimeUtil.now())
);
```

#### 특수 날짜

```java
LocalDateTime now = DateTimeUtil.now();

// 월 첫날 (1일 00:00:00)
LocalDateTime monthStart = DateTimeUtil.startOfMonth(now);

// 월 마지막날 (말일 23:59:59)
LocalDateTime monthEnd = DateTimeUtil.endOfMonth(now);

// 년도 첫날 (1월 1일 00:00:00)
LocalDateTime yearStart = DateTimeUtil.startOfYear(now);

// 년도 마지막날 (12월 31일 23:59:59)
LocalDateTime yearEnd = DateTimeUtil.endOfYear(now);

// 하루 시작 (00:00:00)
LocalDateTime dayStart = DateTimeUtil.startOfDay(now);

// 하루 끝 (23:59:59)
LocalDateTime dayEnd = DateTimeUtil.endOfDay(now);
```

#### 유용한 체크

```java
LocalDateTime target = DateTimeUtil.parse("2024-01-01 10:00:00");

boolean isPast = DateTimeUtil.isPast(target);      // 과거인가?
boolean isFuture = DateTimeUtil.isFuture(target);  // 미래인가?
boolean isToday = DateTimeUtil.isToday(target);    // 오늘인가?
```

---

## Paging

### PageRequestDto

클라이언트로부터 페이징 요청을 받을 때 사용:

```java
@GetMapping("/transactions")
public ApiResponse<PageResponseDto<Transaction>> getTransactions(
    PageRequestDto pageRequest
) {
    // Spring Data Pageable로 변환
    Pageable pageable = pageRequest.toPageable();

    // Repository 호출
    Page<Transaction> page = transactionRepository.findAll(pageable);

    // PageResponseDto로 변환
    return ApiResponse.success(PageResponseDto.of(page));
}
```

**요청 예시:**
```
GET /api/v1/transactions?page=0&size=20&sortBy=createdAt&direction=desc
```

### PageResponseDto

```java
// 1. Spring Data Page에서 직접 변환
Page<Transaction> page = repository.findAll(pageable);
PageResponseDto<Transaction> response = PageResponseDto.of(page);

// 2. Entity → DTO 변환하면서 페이징
Page<Transaction> page = repository.findAll(pageable);
PageResponseDto<TransactionDto> response = PageResponseDto.of(
    page,
    TransactionDto::from
);

// 3. 간단한 페이징 응답 생성
List<Transaction> content = List.of(...);
PageResponseDto<Transaction> response = PageResponseDto.simple(
    content,
    0,      // page
    20,     // size
    100     // totalElements
);

// 4. 빈 페이지
PageResponseDto<Transaction> empty = PageResponseDto.empty();
```

**응답 예시:**
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false,
  "numberOfElements": 20,
  "empty": false
}
```

---

## CommonConstants

애플리케이션 전반에서 사용되는 상수:

```java
// API 관련
CommonConstants.API_VERSION                    // "/api/v1"

// 페이징
CommonConstants.DEFAULT_PAGE                   // 0
CommonConstants.DEFAULT_PAGE_SIZE              // 20
CommonConstants.MAX_PAGE_SIZE                  // 100

// 날짜/시간
CommonConstants.DATE_FORMAT                    // "yyyy-MM-dd"
CommonConstants.DATETIME_FORMAT                // "yyyy-MM-dd HH:mm:ss"

// 인증
CommonConstants.AUTH_HEADER                    // "Authorization"
CommonConstants.TOKEN_PREFIX                   // "Bearer "

// Kafka 토픽
CommonConstants.TOPIC_USER_EVENTS              // "user.events"
CommonConstants.TOPIC_TRANSACTION_EVENTS       // "transaction.events"

// 검증
CommonConstants.EMAIL_REGEX                    // 이메일 정규식
CommonConstants.PHONE_REGEX                    // 전화번호 정규식
CommonConstants.PASSWORD_MIN_LENGTH            // 8
```

---

## 실전 예제

### 예제 1: 사용자 조회 API

```java
@GetMapping("/users/{id}")
public ApiResponse<UserDto> getUser(@PathVariable String id) {
    // 1. 사용자 조회
    User user = userRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 2. DTO 변환
    UserDto userDto = UserDto.from(user);

    // 3. 성공 응답
    return ApiResponse.success(userDto);
}
```

### 예제 2: 사용자 생성 API

```java
@PostMapping("/users")
public ApiResponse<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
    // 1. 이메일 중복 체크
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    // 2. 사용자 생성
    User user = User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .createdAt(DateTimeUtil.now())
        .build();

    userRepository.save(user);

    // 3. 성공 응답
    return ApiResponse.success(UserDto.from(user), "사용자가 생성되었습니다");
}
```

### 예제 3: 거래 내역 조회 API (페이징)

```java
@GetMapping("/transactions")
public ApiResponse<PageResponseDto<TransactionDto>> getTransactions(
    @RequestParam String ledgerId,
    PageRequestDto pageRequest
) {
    // 1. 가계부 접근 권한 확인
    Ledger ledger = ledgerRepository.findById(ledgerId)
        .orElseThrow(() -> new BusinessException(ErrorCode.LEDGER_NOT_FOUND));

    if (!ledger.hasAccessPermission(currentUserId)) {
        throw new BusinessException(ErrorCode.LEDGER_ACCESS_DENIED);
    }

    // 2. 거래 내역 조회 (페이징)
    Pageable pageable = pageRequest.toPageable("createdAt");
    Page<Transaction> page = transactionRepository.findByLedgerId(ledgerId, pageable);

    // 3. DTO 변환 + 페이징 응답
    PageResponseDto<TransactionDto> response = PageResponseDto.of(
        page,
        TransactionDto::from
    );

    return ApiResponse.success(response);
}
```

### 예제 4: 복잡한 Validation

```java
@PostMapping("/transactions")
public ApiResponse<TransactionDto> createTransaction(
    @Valid @RequestBody TransactionCreateRequest request
) {
    // 1. 날짜 검증
    if (DateTimeUtil.isFuture(request.getTransactionDate())) {
        throw new BusinessException(
            ErrorCode.INVALID_TRANSACTION_DATE,
            "거래 날짜는 미래일 수 없습니다"
        );
    }

    // 2. 금액 검증
    if (request.getAmount() <= 0) {
        throw new BusinessException(
            ErrorCode.INVALID_TRANSACTION_AMOUNT,
            "거래 금액은 0보다 커야 합니다"
        );
    }

    if (request.getAmount() > CommonConstants.MAX_TRANSACTION_AMOUNT) {
        throw new BusinessException(
            ErrorCode.INVALID_TRANSACTION_AMOUNT,
            "거래 금액이 너무 큽니다"
        ).addDetail("maxAmount", CommonConstants.MAX_TRANSACTION_AMOUNT)
         .addDetail("requestAmount", request.getAmount());
    }

    // 3. 거래 생성
    Transaction transaction = transactionService.create(request);

    return ApiResponse.success(TransactionDto.from(transaction));
}
```

---

## 테스트

예제 API를 통해 기능을 테스트할 수 있습니다:

```bash
# 성공 응답
curl http://localhost:8080/api/v1/examples/common/success-with-data

# BusinessException
curl http://localhost:8080/api/v1/examples/common/error-basic

# Validation
curl -X POST http://localhost:8080/api/v1/examples/common/validation \
  -H "Content-Type: application/json" \
  -d '{"name":"","email":"invalid","age":-1}'

# DateTimeUtil
curl http://localhost:8080/api/v1/examples/common/datetime-util

# Paging
curl "http://localhost:8080/api/v1/examples/common/paging?page=0&size=10"
```

---

## 정리

모든 파일 위치:
- **ApiResponse**: `common/dto/ApiResponse.java:31`
- **ErrorCode**: `common/exception/ErrorCode.java:20`
- **BusinessException**: `common/exception/BusinessException.java:30`
- **GlobalExceptionHandler**: `common/exception/GlobalExceptionHandler.java:40`
- **DateTimeUtil**: `common/util/DateTimeUtil.java:60`
- **PageRequestDto**: `common/dto/PageRequestDto.java:40`
- **PageResponseDto**: `common/dto/PageResponseDto.java:50`
- **CommonConstants**: `common/constant/CommonConstants.java:15`
- **예제 컨트롤러**: `controller/CommonExampleController.java:30`

Happy Coding! 🚀
