package com.hamkkebu.boilerplate.common.ledger.repository;

import com.hamkkebu.boilerplate.common.enums.ShareStatus;
import com.hamkkebu.boilerplate.common.ledger.entity.SyncedLedgerShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * 동기화된 가계부 공유 Repository 인터페이스
 *
 * <p>각 서비스에서 상속받아 사용하는 공통 Repository입니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Repository
 * public interface LedgerShareRepository extends SyncedLedgerShareRepository<LedgerShare> {
 *     // 서비스별 추가 메서드 정의 가능
 * }
 * }
 * </pre>
 *
 * @param <T> SyncedLedgerShare를 상속받은 엔티티 타입
 */
@NoRepositoryBean
public interface SyncedLedgerShareRepository<T extends SyncedLedgerShare> extends JpaRepository<T, Long> {

    /**
     * 가계부 ID로 공유 목록 조회 (삭제되지 않은 것만)
     *
     * @param ledgerId 가계부 ID
     * @return 해당 가계부의 공유 목록
     */
    List<T> findByLedgerIdAndIsDeletedFalse(Long ledgerId);

    /**
     * 공유 수신자 ID와 상태로 공유 목록 조회 (삭제되지 않은 것만)
     *
     * @param sharedUserId 공유 수신자 ID
     * @param status       공유 상태
     * @return 해당 사용자의 공유 목록
     */
    List<T> findBySharedUserIdAndStatusAndIsDeletedFalse(Long sharedUserId, ShareStatus status);

    /**
     * 가계부 ID와 공유 수신자 ID로 공유 조회 (삭제되지 않은 것만)
     *
     * @param ledgerId     가계부 ID
     * @param sharedUserId 공유 수신자 ID
     * @return 공유 Optional
     */
    Optional<T> findByLedgerIdAndSharedUserIdAndIsDeletedFalse(Long ledgerId, Long sharedUserId);

    /**
     * 가계부 ID, 공유 수신자 ID, 상태로 공유 조회 (삭제되지 않은 것만)
     *
     * @param ledgerId     가계부 ID
     * @param sharedUserId 공유 수신자 ID
     * @param status       공유 상태
     * @return 공유 Optional
     */
    Optional<T> findByLedgerIdAndSharedUserIdAndStatusAndIsDeletedFalse(
            Long ledgerId, Long sharedUserId, ShareStatus status);

    /**
     * 가계부 소유자 ID로 공유 목록 조회 (삭제되지 않은 것만)
     *
     * @param ownerId 소유자 ID
     * @return 해당 소유자가 생성한 공유 목록
     */
    List<T> findByOwnerIdAndIsDeletedFalse(Long ownerId);

    /**
     * 가계부 ID와 상태로 공유 수 조회 (삭제되지 않은 것만)
     *
     * @param ledgerId 가계부 ID
     * @param status   공유 상태
     * @return 공유 수
     */
    Long countByLedgerIdAndStatusAndIsDeletedFalse(Long ledgerId, ShareStatus status);

    /**
     * 특정 가계부가 특정 사용자에게 공유(수락됨)되었는지 확인
     *
     * @param ledgerId     가계부 ID
     * @param sharedUserId 공유 수신자 ID
     * @param status       공유 상태
     * @return 존재하면 true
     */
    boolean existsByLedgerIdAndSharedUserIdAndStatusAndIsDeletedFalse(
            Long ledgerId, Long sharedUserId, ShareStatus status);
}
