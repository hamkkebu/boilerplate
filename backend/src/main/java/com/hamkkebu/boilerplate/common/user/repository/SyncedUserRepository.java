package com.hamkkebu.boilerplate.common.user.repository;

import com.hamkkebu.boilerplate.common.user.entity.SyncedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * 동기화된 사용자 Repository 인터페이스
 *
 * <p>각 서비스에서 상속받아 사용하는 공통 Repository입니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Repository
 * public interface LedgerUserRepository extends SyncedUserRepository<LedgerUser> {
 *     // 서비스별 추가 메서드 정의 가능
 * }
 * }
 * </pre>
 *
 * @param <T> SyncedUser를 상속받은 엔티티 타입
 */
@NoRepositoryBean
public interface SyncedUserRepository<T extends SyncedUser> extends JpaRepository<T, Long> {

    /**
     * userId와 삭제 여부로 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 Optional
     */
    Optional<T> findByUserIdAndIsDeletedFalse(Long userId);

    /**
     * username과 삭제 여부로 사용자 조회
     *
     * @param username 사용자명
     * @return 사용자 Optional
     */
    Optional<T> findByUsernameAndIsDeletedFalse(String username);

    /**
     * userId로 사용자 존재 여부 확인 (삭제되지 않은 사용자)
     *
     * @param userId 사용자 ID
     * @return 존재하면 true
     */
    boolean existsByUserIdAndIsDeletedFalse(Long userId);

    /**
     * 최대 userId 조회 (JIT Provisioning용)
     *
     * @return 최대 userId Optional
     */
    @Query("SELECT MAX(u.userId) FROM #{#entityName} u")
    Optional<Long> findMaxUserId();
}
