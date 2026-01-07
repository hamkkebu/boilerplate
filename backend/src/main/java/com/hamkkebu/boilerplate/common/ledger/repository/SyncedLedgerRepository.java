package com.hamkkebu.boilerplate.common.ledger.repository;

import com.hamkkebu.boilerplate.common.ledger.entity.SyncedLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * 동기화된 가계부 Repository 인터페이스
 *
 * <p>각 서비스에서 상속받아 사용하는 공통 Repository입니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Repository
 * public interface LedgerRepository extends SyncedLedgerRepository<Ledger> {
 *     // 서비스별 추가 메서드 정의 가능
 * }
 * }
 * </pre>
 *
 * @param <T> SyncedLedger를 상속받은 엔티티 타입
 */
@NoRepositoryBean
public interface SyncedLedgerRepository<T extends SyncedLedger> extends JpaRepository<T, Long> {

    /**
     * ledgerId와 삭제 여부로 가계부 조회
     *
     * @param ledgerId 가계부 ID
     * @return 가계부 Optional
     */
    Optional<T> findByLedgerIdAndIsDeletedFalse(Long ledgerId);

    /**
     * ledgerId와 userId, 삭제 여부로 가계부 조회 (소유권 검증 포함)
     *
     * @param ledgerId 가계부 ID
     * @param userId   사용자 ID
     * @return 가계부 Optional
     */
    Optional<T> findByLedgerIdAndUserIdAndIsDeletedFalse(Long ledgerId, Long userId);

    /**
     * userId로 가계부 목록 조회 (삭제되지 않은 가계부)
     *
     * @param userId 사용자 ID
     * @return 가계부 목록
     */
    List<T> findByUserIdAndIsDeletedFalse(Long userId);

    /**
     * ledgerId로 가계부 존재 여부 확인 (삭제되지 않은 가계부)
     *
     * @param ledgerId 가계부 ID
     * @return 존재하면 true
     */
    boolean existsByLedgerIdAndIsDeletedFalse(Long ledgerId);

    /**
     * ledgerId와 userId로 가계부 소유권 확인 (삭제되지 않은 가계부)
     *
     * @param ledgerId 가계부 ID
     * @param userId   사용자 ID
     * @return 해당 사용자의 가계부이면 true
     */
    boolean existsByLedgerIdAndUserIdAndIsDeletedFalse(Long ledgerId, Long userId);
}
