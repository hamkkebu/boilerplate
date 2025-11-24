package com.hamkkebu.boilerplate.service;

import java.util.List;

import com.hamkkebu.boilerplate.common.dto.PageRequestDto;
import com.hamkkebu.boilerplate.common.dto.PageResponseDto;
import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import com.hamkkebu.boilerplate.common.security.PasswordValidator;
import com.hamkkebu.boilerplate.common.security.RefreshTokenWhitelistService;
import com.hamkkebu.boilerplate.data.dto.SampleRequest;
import com.hamkkebu.boilerplate.data.dto.SampleResponse;
import com.hamkkebu.boilerplate.data.entity.Sample;
import com.hamkkebu.boilerplate.data.event.SampleEvent;
import com.hamkkebu.boilerplate.data.mapper.SampleMapper;
import com.hamkkebu.boilerplate.repository.SampleJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final RefreshTokenWhitelistService refreshTokenWhitelistService;

    /**
     * Sample ID 중복 확인
     *
     * @param sampleId 확인할 Sample ID
     * @return 중복이면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public boolean isIdDuplicate(String sampleId) {
        return repository.findBySampleIdAndIsDeletedFalse(sampleId).isPresent();
    }

    /**
     * Sample 닉네임 중복 확인
     *
     * @param sampleNickname 확인할 Sample 닉네임
     * @return 중복이면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public boolean isNicknameDuplicate(String sampleNickname) {
        return repository.findBySampleNicknameAndIsDeletedFalse(sampleNickname).isPresent();
    }

    /**
     * Sample 생성
     *
     * <p>Race Condition 방어:</p>
     * <ul>
     *   <li>DB 레벨: 조건부 UNIQUE 제약조건으로 중복 가입 차단 (idx_sample_id_active, idx_sample_nickname_active)</li>
     *   <li>애플리케이션 레벨: 사전 중복 체크로 불필요한 DB 저장 시도 방지 (성능 최적화)</li>
     *   <li>동시 가입 시도 시: DataIntegrityViolationException catch 후 USER_ALREADY_EXISTS 반환</li>
     * </ul>
     *
     * <p>처리 순서:</p>
     * <ol>
     *   <li>비밀번호 형식 검증</li>
     *   <li>sampleId/nickname 중복 체크 (애플리케이션 레벨 - 빠른 실패)</li>
     *   <li>엔티티 생성 및 저장 시도</li>
     *   <li>DataIntegrityViolationException 발생 시 → USER_ALREADY_EXISTS 처리 (Race Condition 케이스)</li>
     * </ol>
     *
     * @param requestDto Sample 생성 요청
     * @return 생성된 Sample
     * @throws BusinessException 이미 존재하는 sampleId 또는 nickname인 경우, 비밀번호 형식이 올바르지 않은 경우
     */
    @Transactional
    public SampleResponse createSample(SampleRequest requestDto) {
        // 비밀번호 형식 검증 (8자 이상, 영문+숫자+특수문자)
        passwordValidator.validatePasswordFormat(requestDto.getPassword());

        // sampleId 중복 체크
        // SECURITY: 상세 정보 노출 방지 (User Enumeration 방지)
        if (repository.findBySampleIdAndIsDeletedFalse(requestDto.getUsername()).isPresent()) {
            throw new BusinessException(
                ErrorCode.USER_ALREADY_EXISTS,
                "이미 존재하는 사용자 ID입니다"
            );
        }

        // nickname 중복 체크
        // SECURITY: 상세 정보 노출 방지
        if (repository.findBySampleNicknameAndIsDeletedFalse(requestDto.getNickname()).isPresent()) {
            throw new BusinessException(
                ErrorCode.USER_ALREADY_EXISTS,
                "이미 존재하는 닉네임입니다"
            );
        }

        // DTO -> Entity 변환
        Sample entity = mapper.toEntity(requestDto);

        // 비밀번호 암호화 후 설정
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        entity.updatePassword(encodedPassword);

        try {
            repository.save(entity);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // DB 제약조건 위반 (UNIQUE 제약조건이 있는 경우)
            // Race condition으로 인한 중복 생성 시도를 catch
            log.warn("Data integrity violation during sample creation: sampleId={}, error={}",
                requestDto.getUsername(), e.getMessage());
            throw new BusinessException(
                ErrorCode.USER_ALREADY_EXISTS,
                "이미 존재하는 사용자 정보입니다"
            );
        }

        // 이벤트 발행
        publisher.publishEvent(new SampleEvent(
            entity.getSampleId(),
            entity.getSampleFirstName(),
            entity.getSampleLastName()
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
    @Transactional(readOnly = true)
    public SampleResponse getSampleInfo(String sampleId) {
        Sample entity = repository.findBySampleIdAndIsDeletedFalse(sampleId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Sample을 찾을 수 없습니다: sampleId=" + sampleId
            ));

        return mapper.toDto(entity);
    }

    /**
     * RBAC: 전체 Sample 조회 (ADMIN 전용)
     *
     * <p>SECURITY: ADMIN 권한 필요</p>
     * <p>Controller에서 @PreAuthorize("hasRole('ROLE_ADMIN')")로 권한 체크</p>
     *
     * @return 전체 Sample 목록
     */
    @Transactional(readOnly = true)
    public List<SampleResponse> getAllSampleInfo() {
        log.info("Getting all sample info (ADMIN only)");
        List<Sample> samples = repository.findByIsDeletedFalse();
        return samples.stream()
            .map(mapper::toDto)
            .toList();
    }

    /**
     * RBAC: 전체 Sample 페이징 조회 (ADMIN 전용)
     *
     * <p>SECURITY: ADMIN 권한 필요</p>
     * <p>Controller에서 @PreAuthorize("hasRole('ROLE_ADMIN')")로 권한 체크</p>
     *
     * @param pageRequest 페이지 요청 정보
     * @return 페이징된 Sample 목록
     */
    @Transactional(readOnly = true)
    public PageResponseDto<SampleResponse> getAllSampleInfoWithPaging(PageRequestDto pageRequest) {
        log.info("Getting sample info with paging (ADMIN only): page={}, size={}", pageRequest.getPage(), pageRequest.getSize());
        Pageable pageable = pageRequest.toPageable();
        Page<Sample> page = repository.findByIsDeletedFalse(pageable);

        return PageResponseDto.of(page, mapper::toDto);
    }

    /**
     * Sample 삭제 (Soft Delete)
     *
     * <p>물리적 삭제 대신 논리적 삭제를 수행합니다.</p>
     * <p>BaseEntity의 delete() 메서드를 호출하여 isDeleted 플래그를 true로 설정합니다.</p>
     * <p>비밀번호를 검증한 후 삭제를 수행합니다.</p>
     * <p>회원 탈퇴 시 refreshToken을 Whitelist에서 제거합니다.</p>
     *
     * @param sampleId Sample ID
     * @param password 비밀번호
     * @param refreshToken 리프레시 토큰 (Whitelist에서 제거, optional)
     * @throws BusinessException Sample을 찾을 수 없거나 비밀번호가 일치하지 않는 경우
     */
    @Transactional
    public void deleteSample(String sampleId, String password, String refreshToken) {
        // Sample 조회
        Sample sample = repository.findBySampleIdAndIsDeletedFalse(sampleId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "삭제할 Sample을 찾을 수 없습니다: sampleId=" + sampleId
            ));

        // 비밀번호 검증
        passwordValidator.validatePassword(password, sample.getSamplePassword(), sampleId);

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
