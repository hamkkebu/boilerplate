package com.hamkkebu.boilerplate.service;

import java.util.List;

import com.hamkkebu.boilerplate.common.dto.PageRequestDto;
import com.hamkkebu.boilerplate.common.dto.PageResponseDto;
import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import com.hamkkebu.boilerplate.data.dto.RequestSample;
import com.hamkkebu.boilerplate.data.dto.ResponseSample;
import com.hamkkebu.boilerplate.data.entity.Sample;
import com.hamkkebu.boilerplate.data.event.SampleEvent;
import com.hamkkebu.boilerplate.data.mapper.SampleMapper;
import com.hamkkebu.boilerplate.repository.SampleJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Sample Service
 *
 * <p>공통 기능 적용:</p>
 * <ul>
 *   <li>BusinessException: 표준화된 예외 처리</li>
 *   <li>ErrorCode: 명확한 에러 코드</li>
 *   <li>Paging: 페이징 처리 지원</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SampleService {

    private final SampleMapper mapper;
    private final SampleJpaRepository repository;
    private final ApplicationEventPublisher publisher;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.hamkkebu.boilerplate.common.security.RefreshTokenWhitelistService refreshTokenWhitelistService;

    /**
     * Sample ID 중복 확인
     *
     * @param sampleId 확인할 Sample ID
     * @return 중복이면 true, 아니면 false
     */
    public boolean checkSampleIdDuplicate(String sampleId) {
        return repository.findBySampleId(sampleId).isPresent();
    }

    /**
     * Sample 닉네임 중복 확인
     *
     * @param sampleNickname 확인할 Sample 닉네임
     * @return 중복이면 true, 아니면 false
     */
    public boolean checkSampleNicknameDuplicate(String sampleNickname) {
        return repository.findBySampleNicknameAndDeletedFalse(sampleNickname).isPresent();
    }

    /**
     * Sample 생성
     *
     * @param requestDto Sample 생성 요청
     * @return 생성된 Sample
     * @throws BusinessException 이미 존재하는 sampleId인 경우
     */
    @Transactional
    public ResponseSample createSample(RequestSample requestDto) {
        // 중복 체크
        if (repository.findBySampleId(requestDto.getSampleId()).isPresent()) {
            throw new BusinessException(
                ErrorCode.DUPLICATE_RESOURCE,
                "이미 존재하는 sampleId입니다: " + requestDto.getSampleId()
            );
        }

        // DTO -> Entity 변환
        Sample entity = mapper.toEntity(requestDto);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getSamplePassword());
        entity = Sample.builder()
            .sampleId(entity.getSampleId())
            .sampleFname(entity.getSampleFname())
            .sampleLname(entity.getSampleLname())
            .sampleNickname(entity.getSampleNickname())
            .sampleEmail(entity.getSampleEmail())
            .samplePhone(entity.getSamplePhone())
            .samplePassword(encodedPassword)  // 암호화된 비밀번호 설정
            .sampleCountry(entity.getSampleCountry())
            .sampleCity(entity.getSampleCity())
            .sampleState(entity.getSampleState())
            .sampleStreet1(entity.getSampleStreet1())
            .sampleStreet2(entity.getSampleStreet2())
            .sampleZip(entity.getSampleZip())
            .build();

        repository.save(entity);

        // 이벤트 발행
        publisher.publishEvent(new SampleEvent(
            entity.getSampleId(),
            entity.getSampleFname(),
            entity.getSampleLname()
        ));

        log.info("Sample created: sampleId={}", entity.getSampleId());

        return mapper.toDto(entity);
    }

    /**
     * Sample 단건 조회
     *
     * @param sampleId Sample ID
     * @return Sample 정보
     * @throws BusinessException Sample을 찾을 수 없는 경우
     */
    public ResponseSample getSampleInfo(String sampleId) {
        Sample entity = repository.findBySampleId(sampleId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Sample을 찾을 수 없습니다: sampleId=" + sampleId
            ));

        return mapper.toDto(entity);
    }

    /**
     * Sample 전체 조회 (삭제되지 않은 데이터만)
     *
     * @return Sample 목록
     */
    public List<ResponseSample> getAllSampleInfo() {
        return repository.findByDeletedFalse().stream()
            .map(mapper::toDto)
            .toList();
    }

    /**
     * Sample 전체 조회 (페이징, 삭제되지 않은 데이터만)
     *
     * @param pageRequest 페이징 요청
     * @return 페이징된 Sample 목록
     */
    public PageResponseDto<ResponseSample> getAllSampleInfoWithPaging(PageRequestDto pageRequest) {
        // Pageable 생성 (기본 정렬: sampleNum 내림차순)
        Pageable pageable = pageRequest.toPageable("sampleNum");

        // 페이징 조회 (삭제되지 않은 데이터만)
        Page<Sample> page = repository.findByDeletedFalse(pageable);

        // Entity -> DTO 변환 + PageResponseDto 생성
        return PageResponseDto.of(page, mapper::toDto);
    }

    /**
     * Sample 삭제 (Soft Delete)
     *
     * <p>물리적 삭제 대신 논리적 삭제를 수행합니다.</p>
     * <p>BaseEntity의 delete() 메서드를 호출하여 deleted 플래그를 true로 설정합니다.</p>
     * <p>비밀번호를 검증한 후 삭제를 수행합니다.</p>
     * <p>회원 탈퇴 시 refreshToken을 Whitelist에서 제거합니다.</p>
     *
     * @param sampleId Sample ID
     * @param password 비밀번호
     * @param accessToken 액세스 토큰 (사용 안 함)
     * @param refreshToken 리프레시 토큰 (Whitelist에서 제거)
     * @throws BusinessException Sample을 찾을 수 없거나 비밀번호가 일치하지 않는 경우
     */
    @Transactional
    public void deleteSample(String sampleId, String password, String accessToken, String refreshToken) {
        // Sample 조회
        Sample sample = repository.findBySampleIdAndDeletedFalse(sampleId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "삭제할 Sample을 찾을 수 없습니다: sampleId=" + sampleId
            ));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, sample.getSamplePassword())) {
            throw new BusinessException(
                ErrorCode.AUTHENTICATION_FAILED,
                "비밀번호가 일치하지 않습니다"
            );
        }

        // Soft Delete 수행
        sample.delete();

        // 명시적으로 저장 (변경 감지로 자동 저장되지만, 명확성을 위해)
        repository.save(sample);

        log.info("Sample soft deleted: sampleId={}, deletedAt={}", sampleId, sample.getDeletedAt());

        // RefreshToken을 Whitelist에서 제거
        refreshTokenWhitelistService.removeFromWhitelist(sampleId);
        log.info("RefreshToken removed from whitelist for deleted user: {}", sampleId);
    }
}
