package com.hamkkebu.authservice.controller;

import com.hamkkebu.boilerplate.common.constant.CommonConstants;
import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import com.hamkkebu.boilerplate.common.dto.PageRequestDto;
import com.hamkkebu.boilerplate.common.dto.PageResponseDto;
import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import com.hamkkebu.boilerplate.common.util.DateTimeUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 공통 기능 사용 예제 컨트롤러
 *
 * <p>ApiResponse, ErrorCode, BusinessException, DateTimeUtil, Paging 등의 사용 예제를 제공합니다.</p>
 */
@Slf4j
@RestController
@RequestMapping(CommonConstants.API_VERSION + "/examples/common")
public class CommonExampleController {

    // ==================== ApiResponse 예제 ====================

    /**
     * 성공 응답 예제 (데이터 포함)
     * GET /api/v1/examples/common/success-with-data
     */
    @GetMapping("/success-with-data")
    public ApiResponse<ExampleDto> successWithData() {
        ExampleDto data = ExampleDto.builder()
            .id("ex-001")
            .name("예제 데이터")
            .description("ApiResponse 사용 예제")
            .createdAt(DateTimeUtil.now())
            .build();

        return ApiResponse.success(data, "데이터 조회 성공");
    }

    /**
     * 성공 응답 예제 (데이터 없음)
     * POST /api/v1/examples/common/success-without-data
     */
    @PostMapping("/success-without-data")
    public ApiResponse<Void> successWithoutData() {
        log.info("작업 완료");
        return ApiResponse.success("작업이 성공적으로 완료되었습니다");
    }

    // ==================== BusinessException 예제 ====================

    /**
     * BusinessException 예제 - 기본
     * GET /api/v1/examples/common/error-basic
     */
    @GetMapping("/error-basic")
    public ApiResponse<Void> errorBasic() {
        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }

    /**
     * BusinessException 예제 - 커스텀 메시지
     * GET /api/v1/examples/common/error-custom-message?resourceId=123
     */
    @GetMapping("/error-custom-message")
    public ApiResponse<Void> errorCustomMessage(@RequestParam String resourceId) {
        throw new BusinessException(
            ErrorCode.RESOURCE_NOT_FOUND,
            String.format("리소스를 찾을 수 없습니다: ID=%s", resourceId)
        );
    }

    /**
     * BusinessException 예제 - 상세 정보 포함
     * GET /api/v1/examples/common/error-with-details
     */
    @GetMapping("/error-with-details")
    public ApiResponse<Void> errorWithDetails() {
        throw new BusinessException(
            ErrorCode.VALIDATION_FAILED,
            "입력값 검증 실패",
            Map.of(
                "email", "이메일 형식이 올바르지 않습니다",
                "age", "나이는 0보다 커야 합니다",
                "name", "이름은 필수입니다"
            )
        );
    }

    // ==================== Validation 예제 ====================

    /**
     * Validation 예제 - @Valid
     * POST /api/v1/examples/common/validation
     * Body: {"name": "", "email": "invalid", "age": -1}
     */
    @PostMapping("/validation")
    public ApiResponse<ExampleDto> validation(@Valid @RequestBody ValidationExampleRequest request) {
        // Validation 통과 시
        ExampleDto data = ExampleDto.builder()
            .id("ex-002")
            .name(request.getName())
            .description("Validation 통과")
            .createdAt(DateTimeUtil.now())
            .build();

        return ApiResponse.success(data);
    }

    // ==================== DateTimeUtil 예제 ====================

    /**
     * DateTimeUtil 예제
     * GET /api/v1/examples/common/datetime-util
     */
    @GetMapping("/datetime-util")
    public ApiResponse<Map<String, Object>> dateTimeUtil() {
        LocalDateTime now = DateTimeUtil.now();

        Map<String, Object> examples = Map.of(
            "현재시각", DateTimeUtil.format(now),
            "7일후", DateTimeUtil.format(DateTimeUtil.plusDays(now, 7)),
            "월초", DateTimeUtil.format(DateTimeUtil.startOfMonth(now)),
            "월말", DateTimeUtil.format(DateTimeUtil.endOfMonth(now)),
            "오늘인가", DateTimeUtil.isToday(now),
            "과거인가", DateTimeUtil.isPast(now.minusDays(1)),
            "미래인가", DateTimeUtil.isFuture(now.plusDays(1))
        );

        return ApiResponse.success(examples, "DateTimeUtil 사용 예제");
    }

    // ==================== Paging 예제 ====================

    /**
     * 페이징 예제
     * GET /api/v1/examples/common/paging?page=0&size=10&sortBy=createdAt&direction=desc
     */
    @GetMapping("/paging")
    public ApiResponse<PageResponseDto<ExampleDto>> paging(PageRequestDto pageRequest) {
        log.info("페이징 요청: {}", pageRequest);

        // 실제로는 Repository에서 조회
        List<ExampleDto> content = List.of(
            ExampleDto.builder().id("1").name("항목1").createdAt(DateTimeUtil.now()).build(),
            ExampleDto.builder().id("2").name("항목2").createdAt(DateTimeUtil.now()).build(),
            ExampleDto.builder().id("3").name("항목3").createdAt(DateTimeUtil.now()).build()
        );

        // 간단한 페이징 응답 생성
        PageResponseDto<ExampleDto> pageResponse = PageResponseDto.simple(
            content,
            pageRequest.getPage(),
            pageRequest.getSize(),
            100 // 전체 데이터 개수
        );

        return ApiResponse.success(pageResponse);
    }

    // ==================== 여러 ErrorCode 테스트 ====================

    /**
     * 다양한 ErrorCode 테스트
     * GET /api/v1/examples/common/error-code?type=USER_NOT_FOUND
     */
    @GetMapping("/error-code")
    public ApiResponse<Void> errorCode(@RequestParam String type) {
        ErrorCode errorCode = switch (type) {
            case "USER_NOT_FOUND" -> ErrorCode.USER_NOT_FOUND;
            case "LEDGER_NOT_FOUND" -> ErrorCode.LEDGER_NOT_FOUND;
            case "TRANSACTION_NOT_FOUND" -> ErrorCode.TRANSACTION_NOT_FOUND;
            case "VALIDATION_FAILED" -> ErrorCode.VALIDATION_FAILED;
            case "ACCESS_DENIED" -> ErrorCode.ACCESS_DENIED;
            default -> ErrorCode.RESOURCE_NOT_FOUND;
        };

        throw new BusinessException(errorCode);
    }

    // ==================== DTO 클래스 ====================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExampleDto {
        private String id;
        private String name;
        private String description;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationExampleRequest {

        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하여야 합니다")
        private String name;

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        private String email;

        @NotNull(message = "나이는 필수입니다")
        @Min(value = 0, message = "나이는 0보다 커야 합니다")
        @Max(value = 150, message = "나이는 150보다 작아야 합니다")
        private Integer age;
    }
}
