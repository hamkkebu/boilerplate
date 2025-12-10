package com.hamkkebu.boilerplate.common.user.entity;

import com.hamkkebu.boilerplate.common.entity.BaseEntity;
import com.hamkkebu.boilerplate.common.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 동기화된 사용자 엔티티 (Auth Service에서 동기화)
 *
 * <p>auth-service에서 Kafka 이벤트를 통해 동기화된 사용자 정보를 저장합니다.</p>
 * <p>각 서비스(ledger-service, transaction-service 등)에서 상속받아 사용합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Entity
 * @Table(name = "tbl_users")
 * public class LedgerUser extends SyncedUser {
 *     // 서비스별 추가 필드 정의 가능
 * }
 * }
 * </pre>
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class SyncedUser extends BaseEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    /**
     * 사용자 정보 업데이트 (gRPC로 받아온 정보로 갱신)
     *
     * @param username  사용자명
     * @param email     이메일
     * @param firstName 이름
     * @param lastName  성
     * @param isActive  활성 여부
     * @param role      역할
     */
    public void updateFromAuthService(String username, String email, String firstName,
                                       String lastName, Boolean isActive, Role role) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.role = role;
    }

    /**
     * 사용자가 활성 상태인지 확인
     *
     * @return 활성 상태이면 true
     */
    public boolean isUserActive() {
        return Boolean.TRUE.equals(isActive) && !isDeleted();
    }
}
