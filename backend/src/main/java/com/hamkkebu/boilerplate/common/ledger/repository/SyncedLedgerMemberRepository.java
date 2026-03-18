package com.hamkkebu.boilerplate.common.ledger.repository;

import com.hamkkebu.boilerplate.common.ledger.entity.SyncedLedgerMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * 동기화된 가계부 멤버 Repository 인터페이스
 *
 * <p>각 서비스에서 상속받아 사용하는 공통 Repository입니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Repository
 * public interface LedgerMemberRepository extends SyncedLedgerMemberRepository<LedgerMember> {
 *     // 서비스별 추가 메서드 정의 가능
 * }
 * }
 * </pre>
 *
 * @param <T> SyncedLedgerMember를 상속받은 엔티티 타입
 */
@NoRepositoryBean
public interface SyncedLedgerMemberRepository<T extends SyncedLedgerMember> extends JpaRepository<T, Long> {

    /**
     * 가계부 ID로 멤버 목록 조회 (삭제되지 않은 것만)
     *
     * @param ledgerId 가계부 ID
     * @return 해당 가계부의 멤버 목록
     */
    List<T> findByLedgerIdAndIsDeletedFalse(Long ledgerId);

    /**
     * 가계부 ID와 계정 ID로 멤버 조회 (삭제되지 않은 것만)
     *
     * @param ledgerId  가계부 ID
     * @param accountId 계정 ID
     * @return 멤버 Optional
     */
    Optional<T> findByLedgerIdAndAccountIdAndIsDeletedFalse(Long ledgerId, Long accountId);

    /**
     * 계정 ID로 멤버 목록 조회 (삭제되지 않은 것만)
     *
     * @param accountId 계정 ID
     * @return 해당 계정의 멤버 목록
     */
    List<T> findByAccountIdAndIsDeletedFalse(Long accountId);

    /**
     * 특정 가계부의 특정 계정이 멤버인지 확인
     *
     * @param ledgerId  가계부 ID
     * @param accountId 계정 ID
     * @return 멤버이면 true
     */
    boolean existsByLedgerIdAndAccountIdAndIsDeletedFalse(Long ledgerId, Long accountId);

    /**
     * 멤버 ID로 멤버 조회 (삭제되지 않은 것만)
     *
     * @param ledgerMemberId 멤버 ID
     * @return 멤버 Optional
     */
    Optional<T> findByLedgerMemberIdAndIsDeletedFalse(Long ledgerMemberId);

    /**
     * 가계부의 멤버 수 조회 (삭제되지 않은 것만)
     *
     * @param ledgerId 가계부 ID
     * @return 멤버 수
     */
    long countByLedgerIdAndIsDeletedFalse(Long ledgerId);
}
