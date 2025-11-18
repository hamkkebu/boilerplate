package com.hamkkebu.boilerplate.controller;

import com.hamkkebu.boilerplate.common.constant.CommonConstants;
import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import com.hamkkebu.boilerplate.common.dto.PageRequestDto;
import com.hamkkebu.boilerplate.common.dto.PageResponseDto;
import com.hamkkebu.boilerplate.data.dto.DeleteSampleRequest;
import com.hamkkebu.boilerplate.data.dto.RequestSample;
import com.hamkkebu.boilerplate.data.dto.ResponseSample;
import com.hamkkebu.boilerplate.service.SampleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sample Controller
 *
 * <p>Sample 관련 API를 제공합니다.</p>
 * <p>공통 기능 적용:</p>
 * <ul>
 *   <li>ApiResponse: 통일된 API 응답 형식</li>
 *   <li>@Valid: 입력값 검증</li>
 *   <li>BusinessException: 예외 처리 (Service에서)</li>
 * </ul>
 */
@Slf4j
@RequestMapping(CommonConstants.API_VERSION + "/samples")
@RequiredArgsConstructor
@RestController
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
    public ApiResponse<ResponseSample> createSample(@Valid @RequestBody RequestSample requestDto) {
        log.info("Creating sample: sampleId={}", requestDto.getSampleId());

        ResponseSample response = service.createSample(requestDto);

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
    public ApiResponse<Boolean> checkSampleIdDuplicate(@PathVariable String sampleId) {
        log.info("Checking sample ID duplicate: sampleId={}", sampleId);

        boolean isDuplicate = service.checkSampleIdDuplicate(sampleId);

        return ApiResponse.success(isDuplicate, isDuplicate ? "사용 중인 아이디입니다" : "사용 가능한 아이디입니다");
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
    public ApiResponse<Boolean> checkSampleNicknameDuplicate(@PathVariable String nickname) {
        log.info("Checking sample nickname duplicate: nickname={}", nickname);

        boolean isDuplicate = service.checkSampleNicknameDuplicate(nickname);

        return ApiResponse.success(isDuplicate, isDuplicate ? "사용 중인 닉네임입니다" : "사용 가능한 닉네임입니다");
    }

    /**
     * Sample 단건 조회
     * GET /api/v1/samples/{sampleId}
     */
    @GetMapping("/{sampleId}")
    public ApiResponse<ResponseSample> getSampleInfo(@PathVariable String sampleId) {
        log.info("Getting sample info: sampleId={}", sampleId);

        ResponseSample response = service.getSampleInfo(sampleId);

        return ApiResponse.success(response);
    }

    /**
     * Sample 전체 조회
     * GET /api/v1/samples
     */
    @GetMapping
    public ApiResponse<List<ResponseSample>> getAllSampleInfo() {
        log.info("Getting all sample info");

        List<ResponseSample> response = service.getAllSampleInfo();

        return ApiResponse.success(response);
    }

    /**
     * Sample 전체 조회 (페이징)
     * GET /api/v1/samples/page?page=0&size=20&sortBy=sampleNum&direction=desc
     */
    @GetMapping("/page")
    public ApiResponse<PageResponseDto<ResponseSample>> getAllSampleInfoWithPaging(PageRequestDto pageRequest) {
        log.info("Getting all sample info with paging: {}", pageRequest);

        PageResponseDto<ResponseSample> response = service.getAllSampleInfoWithPaging(pageRequest);

        return ApiResponse.success(response);
    }

    /**
     * Sample 삭제 (비밀번호 검증)
     * DELETE /api/v1/samples/{sampleId}
     */
    @DeleteMapping("/{sampleId}")
    public ApiResponse<Void> deleteSample(
            @PathVariable String sampleId,
            @Valid @RequestBody DeleteSampleRequest request,
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        log.info("Deleting sample with password verification: sampleId={}", sampleId);

        // "Bearer " 접두사 제거
        String jwtAccessToken = null;
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            jwtAccessToken = accessToken.substring(7);
        }

        service.deleteSample(sampleId, request.getPassword(), jwtAccessToken, refreshToken);

        return ApiResponse.success("Sample이 성공적으로 삭제되었습니다");
    }
}
