package com.hamkkebu.boilerplate.common.exception;

import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 전역 예외 핸들러
 *
 * <p>애플리케이션에서 발생하는 모든 예외를 통일된 형식으로 처리합니다.</p>
 *
 * <p>처리하는 예외:</p>
 * <ul>
 *   <li>BusinessException: 비즈니스 로직 예외</li>
 *   <li>Validation 예외: @Valid, @Validated, @PathVariable/@RequestParam 검증</li>
 *   <li>HTTP 관련 예외: 잘못된 요청, 지원하지 않는 메서드 등</li>
 *   <li>Security 예외: 접근 거부</li>
 *   <li>Database 예외: 데이터 무결성 위반, DB 접근 오류</li>
 *   <li>기타 예외: 예상치 못한 시스템 예외</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     *
     * <p>비즈니스 로직에서 발생하는 예외</p>
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("BusinessException occurred: code={}, message={}, details={}",
            ex.getErrorCode().getCode(), ex.getErrorMessage(), ex.getDetails());

        ApiResponse<Void> response = ApiResponse.error(
            ex.getErrorCode().getCode(),
            ex.getErrorMessage(),
            ex.getDetails().isEmpty() ? null : ex.getDetails()
        );

        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(response);
    }

    /**
     * @Valid, @Validated 검증 실패 처리 (RequestBody)
     *
     * <p>JSON 요청 본문 검증 실패</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex
    ) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError
                ? ((FieldError) error).getField()
                : "general";
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.VALIDATION_FAILED.getCode(),
            ErrorCode.VALIDATION_FAILED.getMessage(),
            errors
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * @Valid, @Validated 검증 실패 처리 (RequestParam, ModelAttribute)
     *
     * <p>쿼리 파라미터나 폼 데이터 검증 실패</p>
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
        log.warn("Binding validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError
                ? ((FieldError) error).getField()
                : "general";
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.VALIDATION_FAILED.getCode(),
            ErrorCode.VALIDATION_FAILED.getMessage(),
            errors
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * @Validated 검증 실패 처리 (PathVariable, RequestParam)
     *
     * <p>경로 변수나 쿼리 파라미터의 @Size, @NotNull 등 검증 실패</p>
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
        ConstraintViolationException ex
    ) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (existing, replacement) -> existing  // 중복 시 기존 값 유지
            ));

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.VALIDATION_FAILED.getCode(),
            ErrorCode.VALIDATION_FAILED.getMessage(),
            errors
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 필수 파라미터 누락 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException ex
    ) {
        log.warn("Missing request parameter: {}", ex.getParameterName());

        String message = String.format("필수 파라미터가 누락되었습니다: %s (%s)",
            ex.getParameterName(), ex.getParameterType());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.MISSING_PARAMETER.getCode(),
            message,
            Map.of("parameter", ex.getParameterName(), "type", ex.getParameterType())
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 필수 헤더 누락 처리
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestHeaderException(
        MissingRequestHeaderException ex
    ) {
        log.warn("Missing request header: {}", ex.getHeaderName());

        String message = String.format("필수 헤더가 누락되었습니다: %s", ex.getHeaderName());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.MISSING_PARAMETER.getCode(),
            message,
            Map.of("header", ex.getHeaderName())
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 파라미터 타입 불일치 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException ex
    ) {
        log.warn("Method argument type mismatch: name={}, value={}, requiredType={}",
            ex.getName(), ex.getValue(), ex.getRequiredType());

        String message = String.format("파라미터 타입이 올바르지 않습니다: %s (expected: %s, actual: %s)",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
            ex.getValue()
        );

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INVALID_PARAMETER_FORMAT.getCode(),
            message,
            Map.of("parameter", ex.getName(), "value", ex.getValue())
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * HTTP 메시지 읽기 실패 처리 (JSON 파싱 에러 등)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex
    ) {
        log.warn("HTTP message not readable: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INVALID_REQUEST.getCode(),
            "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요"
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 지원하지 않는 HTTP 메서드 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException ex
    ) {
        log.warn("HTTP method not supported: {}", ex.getMethod());

        String message = String.format("지원하지 않는 HTTP 메서드입니다: %s (지원: %s)",
            ex.getMethod(), ex.getSupportedHttpMethods());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.METHOD_NOT_ALLOWED.getCode(),
            message,
            Map.of("method", ex.getMethod(), "supported", ex.getSupportedHttpMethods())
        );

        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(response);
    }

    /**
     * 지원하지 않는 미디어 타입 처리
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException ex
    ) {
        log.warn("HTTP media type not supported: {}", ex.getContentType());

        String message = String.format("지원하지 않는 미디어 타입입니다: %s (지원: %s)",
            ex.getContentType(), ex.getSupportedMediaTypes());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(),
            message,
            Map.of("contentType", ex.getContentType(), "supported", ex.getSupportedMediaTypes())
        );

        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(response);
    }

    /**
     * 404 Not Found 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        String message = String.format("요청한 리소스를 찾을 수 없습니다: %s %s",
            ex.getHttpMethod(), ex.getRequestURL());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.RESOURCE_NOT_FOUND.getCode(),
            message,
            Map.of("method", ex.getHttpMethod(), "url", ex.getRequestURL())
        );

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(response);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INVALID_REQUEST.getCode(),
            ex.getMessage() != null ? ex.getMessage() : "잘못된 요청입니다"
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * IllegalStateException 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.BUSINESS_RULE_VIOLATION.getCode(),
            ex.getMessage() != null ? ex.getMessage() : "비즈니스 규칙을 위반했습니다"
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * Spring Security AccessDeniedException 처리
     *
     * <p>접근 권한이 없는 리소스에 접근하려 할 때</p>
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.ACCESS_DENIED.getCode(),
            ErrorCode.ACCESS_DENIED.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(response);
    }

    /**
     * DataIntegrityViolationException 처리
     *
     * <p>데이터베이스 무결성 제약 위반 (UNIQUE, FOREIGN KEY 등)</p>
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
        DataIntegrityViolationException ex
    ) {
        log.warn("Data integrity violation: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.DUPLICATE_RESOURCE.getCode(),
            "데이터 무결성 제약을 위반했습니다. 중복된 데이터가 있는지 확인해주세요."
        );

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(response);
    }

    /**
     * DataAccessException 처리
     *
     * <p>데이터베이스 접근 중 발생하는 일반적인 에러</p>
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException ex) {
        log.error("Database access error", ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.DATABASE_ERROR.getCode(),
            ErrorCode.DATABASE_ERROR.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }

    /**
     * MaxUploadSizeExceededException 처리
     *
     * <p>파일 업로드 크기 제한 초과</p>
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(
        MaxUploadSizeExceededException ex
    ) {
        log.warn("Max upload size exceeded: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INVALID_REQUEST.getCode(),
            "업로드 파일 크기가 제한을 초과했습니다"
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * NullPointerException 처리
     *
     * <p>예상치 못한 null 값 접근</p>
     * <p>NOTE: 이 예외는 발생하지 않도록 하는 것이 좋습니다</p>
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointerException(NullPointerException ex) {
        log.error("NullPointerException occurred - this should not happen!", ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            "서버 내부 오류가 발생했습니다"
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }

    /**
     * 예상치 못한 예외 처리 (최후의 보루)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unexpected exception occurred", ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }
}
