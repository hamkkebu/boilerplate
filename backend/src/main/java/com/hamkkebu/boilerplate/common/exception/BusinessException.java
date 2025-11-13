package com.hamkkebu.boilerplate.common.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 비즈니스 예외 클래스
 *
 * <p>비즈니스 로직에서 발생하는 예외를 표준화된 형태로 처리합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // 1. 기본 사용
 * throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 *
 * // 2. 커스텀 메시지
 * throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자 ID: " + userId + "를 찾을 수 없습니다");
 *
 * // 3. 상세 정보 포함
 * throw new BusinessException(ErrorCode.VALIDATION_FAILED, "입력값 검증 실패",
 *     Map.of("field", "email", "value", "invalid-email"));
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 에러 코드
     */
    private final ErrorCode errorCode;

    /**
     * 커스텀 에러 메시지
     * (null인 경우 ErrorCode의 기본 메시지 사용)
     */
    private final String customMessage;

    /**
     * 에러 상세 정보
     * (필드별 validation 에러, 추가 디버그 정보 등)
     */
    private final Map<String, Object> details;

    /**
     * 기본 생성자 - ErrorCode만 사용
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
        this.details = new HashMap<>();
    }

    /**
     * 커스텀 메시지와 함께 생성
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.details = new HashMap<>();
    }

    /**
     * 상세 정보와 함께 생성
     */
    public BusinessException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
        this.details = details != null ? details : new HashMap<>();
    }

    /**
     * 커스텀 메시지와 상세 정보와 함께 생성
     */
    public BusinessException(ErrorCode errorCode, String customMessage, Map<String, Object> details) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.details = details != null ? details : new HashMap<>();
    }

    /**
     * 원인 예외와 함께 생성
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.customMessage = null;
        this.details = new HashMap<>();
    }

    /**
     * 커스텀 메시지와 원인 예외와 함께 생성
     */
    public BusinessException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.details = new HashMap<>();
    }

    /**
     * 전체 정보와 함께 생성
     */
    public BusinessException(ErrorCode errorCode, String customMessage, Map<String, Object> details, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.details = details != null ? details : new HashMap<>();
    }

    /**
     * 실제 에러 메시지 반환
     * (커스텀 메시지가 있으면 커스텀 메시지, 없으면 ErrorCode의 기본 메시지)
     */
    public String getErrorMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }

    /**
     * 상세 정보 추가
     */
    public BusinessException addDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }

    /**
     * 여러 상세 정보 추가
     */
    public BusinessException addDetails(Map<String, Object> additionalDetails) {
        if (additionalDetails != null) {
            this.details.putAll(additionalDetails);
        }
        return this;
    }

    @Override
    public String toString() {
        return String.format("BusinessException(errorCode=%s, message=%s, details=%s)",
            errorCode.getCode(), getErrorMessage(), details);
    }
}
