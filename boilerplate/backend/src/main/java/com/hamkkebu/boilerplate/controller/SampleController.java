package com.hamkkebu.boilerplate.controller;

import com.hamkkebu.boilerplate.common.constant.CommonConstants;
import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import com.hamkkebu.boilerplate.common.dto.PageRequestDto;
import com.hamkkebu.boilerplate.common.dto.PageResponseDto;
import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import com.hamkkebu.boilerplate.data.dto.DeleteSampleRequest;
import com.hamkkebu.boilerplate.data.dto.DuplicateCheckResponse;
import com.hamkkebu.boilerplate.data.dto.SampleRequest;
import com.hamkkebu.boilerplate.data.dto.SampleResponse;
import com.hamkkebu.boilerplate.service.SampleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sample Controller
 *
 * <p>Sample 관련 API를 제공합니다.</p>
 * <p>공통 기능 적용:</p>
 * <ul>
 *   <li>ApiResponse: 통일된 API 응답 형식</li>
 *   <li>@Valid: 입력값 검증 (RequestBody)</li>
 *   <li>BusinessException: 예외 처리 (Service에서)</li>
 * </ul>
 *
 * <p>Note: @PathVariable 검증:</p>
 * <ul>
 *   <li>PathVariable은 DB 컬럼 길이로 제한됨 (VARCHAR 20/50)</li>
 *   <li>JPA가 SQL Injection 방어</li>
 *   <li>추가 검증 필요 시 Service 레이어에서 처리</li>
 * </ul>
 */
@Slf4j
@RequestMapping(CommonConstants.API_VERSION + "/samples")
@RequiredArgsConstructor
@RestController
@Validated
public class SampleController {

    private final SampleService service;

    /**
     * Sample 생성
     * POST /api/v1/samples
     */
    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 생성합니다.",
        security = {} // 인증 불필요
    )
    @PostMapping
    public ApiResponse<SampleResponse> createSample(@Valid @RequestBody SampleRequest requestDto) {
        log.info("Creating sample: sampleId={}", requestDto.getUsername());

        SampleResponse response = service.createSample(requestDto);

        return ApiResponse.success(response, "Sample이 성공적으로 생성되었습니다");
    }

    /**
     * Sample ID 중복 확인
     * GET /api/v1/samples/check/{sampleId}
     */
    @Operation(
        summary = "아이디 중복 확인",
        description = "사용자 ID의 중복 여부를 확인합니다.",
        security = {} // 인증 불필요
    )
    @GetMapping("/check/{sampleId}")
    public ApiResponse<DuplicateCheckResponse> checkSampleIdDuplicate(
            @PathVariable @Size(min = 3, max = 20, message = "sampleId는 3자 이상 20자 이하여야 합니다") String sampleId) {
        log.info("Checking sample ID duplicate: sampleId={}", sampleId);

        boolean exists = service.isIdDuplicate(sampleId);
        DuplicateCheckResponse response = DuplicateCheckResponse.of(exists, sampleId);

        return ApiResponse.success(response, exists ? "사용 중인 아이디입니다" : "사용 가능한 아이디입니다");
    }

    /**
     * Sample 닉네임 중복 확인
     * GET /api/v1/samples/check/nickname/{nickname}
     */
    @Operation(
        summary = "닉네임 중복 확인",
        description = "닉네임의 중복 여부를 확인합니다.",
        security = {} // 인증 불필요
    )
    @GetMapping("/check/nickname/{nickname}")
    public ApiResponse<DuplicateCheckResponse> checkSampleNicknameDuplicate(
            @PathVariable @Size(min = 2, max = 20, message = "nickname은 2자 이상 20자 이하여야 합니다") String nickname) {
        log.info("Checking sample nickname duplicate: nickname={}", nickname);

        boolean exists = service.isNicknameDuplicate(nickname);
        DuplicateCheckResponse response = DuplicateCheckResponse.of(exists, nickname);

        return ApiResponse.success(response, exists ? "사용 중인 닉네임입니다" : "사용 가능한 닉네임입니다");
    }

    /**
     * Sample 단건 조회 (본인 정보만)
     * GET /api/v1/samples/{sampleId}
     *
     * SECURITY: IDOR 방어 - 본인의 정보만 조회 가능
     */
    @Operation(
        summary = "사용자 정보 조회",
        description = "본인의 사용자 정보를 조회합니다. (인증 필요, 본인만 가능)"
    )
    @GetMapping("/{sampleId}")
    public ApiResponse<SampleResponse> getSampleInfo(
            @PathVariable @Size(min = 3, max = 20, message = "sampleId는 3자 이상 20자 이하여야 합니다") String sampleId,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("Getting sample info: sampleId={}, requestedBy={}", sampleId, userId);

        // SECURITY: IDOR 방어 - 본인 정보만 조회
        if (!userId.equals(sampleId)) {
            log.warn("Unauthorized access attempt: userId={}, targetId={}", userId, sampleId);
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "본인의 정보만 조회할 수 있습니다"
            );
        }

        SampleResponse response = service.getSampleInfo(sampleId);

        return ApiResponse.success(response);
    }

    /**
     * RBAC: 전체 사용자 조회 (ADMIN 전용)
     * GET /api/v1/samples
     *
     * SECURITY: ADMIN 권한이 있는 사용자만 접근 가능
     * - 개인정보 대량 조회는 관리자만 허용
     * - @PreAuthorize로 권한 체크
     */
    @Operation(
        summary = "[ADMIN] 전체 사용자 조회",
        description = "모든 사용자 정보를 조회합니다. ADMIN 권한 필요."
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ApiResponse<List<SampleResponse>> getAllSampleInfo() {
        log.info("Getting all sample info (ADMIN only)");
        List<SampleResponse> response = service.getAllSampleInfo();
        return ApiResponse.success(response);
    }

    /**
     * RBAC: 페이징 사용자 조회 (ADMIN 전용)
     * GET /api/v1/samples/page
     *
     * SECURITY: ADMIN 권한이 있는 사용자만 접근 가능
     */
    @Operation(
        summary = "[ADMIN] 페이징 사용자 조회",
        description = "사용자 정보를 페이징하여 조회합니다. ADMIN 권한 필요."
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/page")
    public ApiResponse<PageResponseDto<SampleResponse>> getAllSampleInfoWithPaging(PageRequestDto pageRequest) {
        log.info("Getting sample info with paging (ADMIN only): page={}, size={}", pageRequest.getPage(), pageRequest.getSize());
        PageResponseDto<SampleResponse> response = service.getAllSampleInfoWithPaging(pageRequest);
        return ApiResponse.success(response);
    }

    /**
     * Sample 삭제 (비밀번호 검증)
     * DELETE /api/v1/samples/{sampleId}
     *
     * <p>Spring Security의 인증 정보를 사용하여 현재 사용자를 확인합니다.</p>
     * <p>Swagger의 Authorize 버튼으로 설정한 JWT 토큰이 자동으로 사용됩니다.</p>
     */
    @DeleteMapping("/{sampleId}")
    public ApiResponse<Void> deleteSample(
            @PathVariable @Size(min = 3, max = 20, message = "sampleId는 3자 이상 20자 이하여야 합니다") String sampleId,
            @Valid @RequestBody DeleteSampleRequest request,
            Authentication authentication,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        String userId = authentication.getName();
        log.info("Deleting sample with password verification: sampleId={}, userId={}", sampleId, userId);

        // 권한 검증: 본인 계정만 삭제 가능
        if (!userId.equals(sampleId)) {
            log.warn("Unauthorized deletion attempt: userId={}, targetId={}", userId, sampleId);
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "본인의 계정만 삭제할 수 있습니다"
            );
        }

        service.deleteSample(sampleId, request.getPassword(), refreshToken);

        return ApiResponse.success("Sample이 성공적으로 삭제되었습니다");
    }
}
