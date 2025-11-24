package com.hamkkebu.boilerplate.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 표준화된 에러 코드
 *
 * <p>에러 코드 규칙: {CATEGORY}-{NUMBER}</p>
 * <ul>
 *   <li>COMMON: 공통 에러 (001-099)</li>
 *   <li>USER: 사용자 관련 (101-199)</li>
 *   <li>LEDGER: 가계부 관련 (201-299)</li>
 *   <li>TRANSACTION: 거래 관련 (301-399)</li>
 *   <li>AUTH: 인증/인가 관련 (401-499)</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ==================== 공통 에러 (COMMON-001 ~ 099) ====================

    /**
     * 요청 성공 (정상)
     */
    SUCCESS("COMMON-000", "Success", HttpStatus.OK),

    /**
     * 알 수 없는 에러
     */
    INTERNAL_SERVER_ERROR("COMMON-001", "내부 서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * 잘못된 요청
     */
    INVALID_REQUEST("COMMON-002", "잘못된 요청입니다", HttpStatus.BAD_REQUEST),

    /**
     * 필수 파라미터 누락
     */
    MISSING_PARAMETER("COMMON-003", "필수 파라미터가 누락되었습니다", HttpStatus.BAD_REQUEST),

    /**
     * 잘못된 파라미터 형식
     */
    INVALID_PARAMETER_FORMAT("COMMON-004", "파라미터 형식이 올바르지 않습니다", HttpStatus.BAD_REQUEST),

    /**
     * 리소스를 찾을 수 없음
     */
    RESOURCE_NOT_FOUND("COMMON-005", "요청한 리소스를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    /**
     * 중복된 리소스
     */
    DUPLICATE_RESOURCE("COMMON-006", "이미 존재하는 리소스입니다", HttpStatus.CONFLICT),

    /**
     * 지원하지 않는 메서드
     */
    METHOD_NOT_ALLOWED("COMMON-007", "지원하지 않는 HTTP 메서드입니다", HttpStatus.METHOD_NOT_ALLOWED),

    /**
     * 지원하지 않는 미디어 타입
     */
    UNSUPPORTED_MEDIA_TYPE("COMMON-008", "지원하지 않는 미디어 타입입니다", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    /**
     * Validation 실패
     */
    VALIDATION_FAILED("COMMON-009", "입력값 검증에 실패했습니다", HttpStatus.BAD_REQUEST),

    /**
     * 비즈니스 로직 위반
     */
    BUSINESS_RULE_VIOLATION("COMMON-010", "비즈니스 규칙을 위반했습니다", HttpStatus.BAD_REQUEST),

    /**
     * 데이터베이스 에러
     */
    DATABASE_ERROR("COMMON-011", "데이터베이스 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * 외부 API 호출 실패
     */
    EXTERNAL_API_ERROR("COMMON-012", "외부 API 호출에 실패했습니다", HttpStatus.BAD_GATEWAY),

    /**
     * Rate Limit 초과
     */
    RATE_LIMIT_EXCEEDED("COMMON-013", "요청 횟수가 너무 많습니다. 잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS),

    // ==================== 사용자 관련 에러 (USER-101 ~ 199) ====================

    /**
     * 사용자를 찾을 수 없음
     */
    USER_NOT_FOUND("USER-101", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    /**
     * 이미 존재하는 사용자
     */
    USER_ALREADY_EXISTS("USER-102", "이미 존재하는 사용자입니다", HttpStatus.CONFLICT),

    /**
     * 비밀번호 불일치
     */
    PASSWORD_MISMATCH("USER-103", "비밀번호가 일치하지 않습니다", HttpStatus.BAD_REQUEST),

    /**
     * 이메일 형식 오류
     */
    INVALID_EMAIL_FORMAT("USER-104", "올바르지 않은 이메일 형식입니다", HttpStatus.BAD_REQUEST),

    /**
     * 이미 존재하는 이메일
     */
    EMAIL_ALREADY_EXISTS("USER-105", "이미 사용 중인 이메일입니다", HttpStatus.CONFLICT),

    /**
     * 사용자 계정 비활성화
     */
    USER_ACCOUNT_DISABLED("USER-106", "비활성화된 계정입니다", HttpStatus.FORBIDDEN),

    /**
     * 사용자 계정 잠김
     */
    USER_ACCOUNT_LOCKED("USER-107", "잠긴 계정입니다", HttpStatus.FORBIDDEN),

    // ==================== 가계부 관련 에러 (LEDGER-201 ~ 299) ====================

    /**
     * 가계부를 찾을 수 없음
     */
    LEDGER_NOT_FOUND("LEDGER-201", "가계부를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    /**
     * 가계부 접근 권한 없음
     */
    LEDGER_ACCESS_DENIED("LEDGER-202", "가계부에 접근할 권한이 없습니다", HttpStatus.FORBIDDEN),

    /**
     * 가계부 이름 중복
     */
    LEDGER_NAME_DUPLICATE("LEDGER-203", "이미 존재하는 가계부 이름입니다", HttpStatus.CONFLICT),

    /**
     * 가계부 최대 개수 초과
     */
    LEDGER_LIMIT_EXCEEDED("LEDGER-204", "생성 가능한 가계부 개수를 초과했습니다", HttpStatus.BAD_REQUEST),

    /**
     * 가계부 삭제 불가 (거래 내역 존재)
     */
    LEDGER_HAS_TRANSACTIONS("LEDGER-205", "거래 내역이 존재하는 가계부는 삭제할 수 없습니다", HttpStatus.BAD_REQUEST),

    // ==================== 거래 관련 에러 (TRANSACTION-301 ~ 399) ====================

    /**
     * 거래를 찾을 수 없음
     */
    TRANSACTION_NOT_FOUND("TRANSACTION-301", "거래를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    /**
     * 거래 접근 권한 없음
     */
    TRANSACTION_ACCESS_DENIED("TRANSACTION-302", "거래에 접근할 권한이 없습니다", HttpStatus.FORBIDDEN),

    /**
     * 잘못된 거래 금액
     */
    INVALID_TRANSACTION_AMOUNT("TRANSACTION-303", "잘못된 거래 금액입니다", HttpStatus.BAD_REQUEST),

    /**
     * 잘못된 거래 날짜
     */
    INVALID_TRANSACTION_DATE("TRANSACTION-304", "잘못된 거래 날짜입니다", HttpStatus.BAD_REQUEST),

    /**
     * 거래 카테고리 없음
     */
    TRANSACTION_CATEGORY_NOT_FOUND("TRANSACTION-305", "거래 카테고리를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    /**
     * 예산 초과
     */
    BUDGET_EXCEEDED("TRANSACTION-306", "예산을 초과했습니다", HttpStatus.BAD_REQUEST),

    // ==================== 인증/인가 관련 에러 (AUTH-401 ~ 499) ====================

    /**
     * 인증 실패
     */
    AUTHENTICATION_FAILED("AUTH-401", "인증에 실패했습니다", HttpStatus.UNAUTHORIZED),

    /**
     * 인증 토큰 없음
     */
    MISSING_TOKEN("AUTH-402", "인증 토큰이 없습니다", HttpStatus.UNAUTHORIZED),

    /**
     * 잘못된 토큰
     */
    INVALID_TOKEN("AUTH-403", "잘못된 인증 토큰입니다", HttpStatus.UNAUTHORIZED),

    /**
     * 만료된 토큰
     */
    EXPIRED_TOKEN("AUTH-404", "만료된 인증 토큰입니다", HttpStatus.UNAUTHORIZED),

    /**
     * 권한 없음
     */
    ACCESS_DENIED("AUTH-405", "접근 권한이 없습니다", HttpStatus.FORBIDDEN),

    /**
     * 리프레시 토큰 없음
     */
    MISSING_REFRESH_TOKEN("AUTH-406", "리프레시 토큰이 없습니다", HttpStatus.UNAUTHORIZED),

    /**
     * 잘못된 리프레시 토큰
     */
    INVALID_REFRESH_TOKEN("AUTH-407", "잘못된 리프레시 토큰입니다", HttpStatus.UNAUTHORIZED),

    /**
     * 만료된 리프레시 토큰
     */
    EXPIRED_REFRESH_TOKEN("AUTH-408", "만료된 리프레시 토큰입니다", HttpStatus.UNAUTHORIZED),

    // ==================== Kafka 관련 에러 (KAFKA-501 ~ 599) ====================

    /**
     * 이벤트 발행 실패
     */
    EVENT_PUBLISH_FAILED("KAFKA-501", "이벤트 발행에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * 이벤트 처리 실패
     */
    EVENT_PROCESSING_FAILED("KAFKA-502", "이벤트 처리에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * 토픽을 찾을 수 없음
     */
    TOPIC_NOT_FOUND("KAFKA-503", "Kafka 토픽을 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    /**
     * 에러 코드 (예: COMMON-001, USER-101)
     */
    private final String code;

    /**
     * 에러 메시지
     */
    private final String message;

    /**
     * HTTP 상태 코드
     */
    private final HttpStatus httpStatus;

    /**
     * HTTP 상태 코드 값 반환
     */
    public int getStatusValue() {
        return httpStatus.value();
    }
}
