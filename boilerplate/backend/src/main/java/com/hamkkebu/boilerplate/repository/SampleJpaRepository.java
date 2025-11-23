package com.hamkkebu.boilerplate.repository;

import com.hamkkebu.boilerplate.data.entity.Sample;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Sample JPA Repository
 *
 * <p>Sample 엔티티에 대한 데이터 접근 인터페이스입니다.</p>
 * <p>Soft Delete가 적용되어 있으므로, 삭제되지 않은 데이터만 조회합니다.</p>
 */
public interface SampleJpaRepository extends JpaRepository<Sample, Long> {

    /**
     * Sample ID로 Sample 조회 (삭제되지 않은 데이터만)
     *
     * @param sampleId Sample ID
     * @return Sample 엔티티 (Optional)
     */
    Optional<Sample> findBySampleIdAndIsDeletedFalse(String sampleId);

    /**
     * Sample 닉네임으로 Sample 조회 (삭제되지 않은 데이터만)
     *
     * @param sampleNickname Sample 닉네임
     * @return Sample 엔티티 (Optional)
     */
    Optional<Sample> findBySampleNicknameAndIsDeletedFalse(String sampleNickname);

    /**
     * 모든 Sample 조회 (삭제되지 않은 데이터만)
     *
     * @return Sample 리스트
     */
    List<Sample> findByIsDeletedFalse();

    /**
     * 페이징된 Sample 조회 (삭제되지 않은 데이터만)
     *
     * @param pageable 페이징 정보
     * @return Sample 페이지
     */
    Page<Sample> findByIsDeletedFalse(Pageable pageable);

    /**
     * 물리적 삭제 메서드 제거
     * <p>Soft Delete 패턴을 사용하므로, 물리적 삭제는 권장하지 않습니다.</p>
     * <p>삭제는 Service 레이어에서 Sample.delete() 메서드를 호출하여 수행합니다.</p>
     */
    // void deleteBySampleId(String sampleId); // 제거됨
}
