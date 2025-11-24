package com.hamkkebu.boilerplate.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 통일된 API 응답 형식
 *
 * <p>모든 API 응답은 이 클래스를 사용하여 일관된 형식을 유지합니다.</p>
 *
 * <p>성공 응답 예시:</p>
 * <pre>
 * {
 *   "success": true,
 *   "data": {...},
 *   "message": "성공적으로 처리되었습니다",
 *   "timestamp": "2024-01-01T10:00:00"
 * }
 * </pre>
 *
 * <p>실패 응답 예시:</p>
 * <pre>
 * {
 *   "success": false,
 *   "error": {
 *     "code": "USER-001",
 *     "message": "사용자를 찾을 수 없습니다",
 *     "details": {...}
 *   },
 *   "timestamp": "2024-01-01T10:00:00"
 * }
 * </pre>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 요청 성공 여부
     */
    private boolean success;

    /**
     * 응답 데이터 (성공 시)
     */
    private T data;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 에러 정보 (실패 시)
     */
    private ErrorResponse error;

    /**
     * 응답 시각
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ==================== 성공 응답 생성 메서드 ====================

    /**
     * 데이터와 함께 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message("Success")
            .build();
    }

    /**
     * 데이터와 메시지와 함께 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .build();
    }

    /**
     * 데이터 없이 성공 메시지만 반환
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .build();
    }

    /**
     * 데이터 없이 성공 응답 (기본 메시지)
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
            .success(true)
            .message("Success")
            .build();
    }

    // ==================== 실패 응답 생성 메서드 ====================

    /**
     * 에러 응답 생성
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(new ErrorResponse(code, message, null))
            .build();
    }

    /**
     * 에러 응답 생성 (상세 정보 포함)
     */
    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(new ErrorResponse(code, message, details))
            .build();
    }

    /**
     * ErrorResponse 객체로 에러 응답 생성
     */
    public static <T> ApiResponse<T> error(ErrorResponse error) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(error)
            .build();
    }

    // ==================== 내부 에러 응답 클래스 ====================

    /**
     * 에러 응답 상세 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorResponse {

        /**
         * 에러 코드 (예: USER-001, LEDGER-002)
         */
        private String code;

        /**
         * 에러 메시지
         */
        private String message;

        /**
         * 에러 상세 정보 (선택적)
         * - 필드별 validation 에러
         * - 추가 디버그 정보 등
         */
        private Object details;
    }
}
