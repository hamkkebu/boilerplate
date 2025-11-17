package com.hamkkebu.boilerplate.controller;

import com.hamkkebu.boilerplate.common.constant.CommonConstants;
import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import com.hamkkebu.boilerplate.common.dto.PageRequestDto;
import com.hamkkebu.boilerplate.common.dto.PageResponseDto;
import com.hamkkebu.boilerplate.data.dto.DeleteSampleRequest;
import com.hamkkebu.boilerplate.data.dto.RequestSample;
import com.hamkkebu.boilerplate.data.dto.ResponseSample;
import com.hamkkebu.boilerplate.service.SampleService;
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
    @PostMapping
    public ApiResponse<ResponseSample> createSample(@Valid @RequestBody RequestSample requestDto) {
        log.info("Creating sample: sampleId={}", requestDto.getSampleId());

        ResponseSample response = service.createSample(requestDto);

        return ApiResponse.success(response, "Sample이 성공적으로 생성되었습니다");
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
            @Valid @RequestBody DeleteSampleRequest request) {
        log.info("Deleting sample with password verification: sampleId={}", sampleId);

        service.deleteSample(sampleId, request.getPassword());

        return ApiResponse.success("Sample이 성공적으로 삭제되었습니다");
    }
}
