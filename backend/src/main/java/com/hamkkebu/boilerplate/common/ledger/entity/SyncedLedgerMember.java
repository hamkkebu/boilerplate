package com.hamkkebu.boilerplate.common.ledger.entity;

import com.hamkkebu.boilerplate.common.entity.BaseEntity;
import com.hamkkebu.boilerplate.common.enums.MemberRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 동기화된 가계부 멤버 엔티티 (Ledger Service에서 동기화)
 *
 * <p>ledger-service에서 Kafka 이벤트를 통해 동기화된 가계부 멤버 정보를 저장합니다.</p>
 * <p>각 서비스(transaction-service 등)에서 상속받아 사용합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Entity
 * @Table(name = "tbl_ledger_members")
 * public class LedgerMember extends SyncedLedgerMember {
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
public abstract class SyncedLedgerMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_member_id", nullable = false)
    private Long ledgerMemberId;

    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;

    @Column(name = "joined_at")
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    // ==================== 상태 확인 메서드 ====================

    /**
     * 특정 사용자가 이 가계부의 멤버인지 확인합니다.
     *
     * @param accountId 계정 ID
     * @return 해당 계정이 멤버이면 true
     */
    public boolean isMember(Long accountId) {
        return this.accountId != null && this.accountId.equals(accountId);
    }

    /**
     * 멤버가 특정 역할을 가지고 있는지 확인합니다.
     *
     * @param role 역할
     * @return 해당 역할을 가지고 있으면 true
     */
    public boolean hasRole(MemberRole role) {
        return this.role != null && this.role.equals(role);
    }

    /**
     * 멤버가 쓰기 권한을 가지고 있는지 확인합니다.
     *
     * @return 쓰기 권한이 있으면 true
     */
    public boolean hasWriteAccess() {
        return this.role != null && this.role.hasWriteAccess();
    }

    /**
     * 멤버가 관리자 권한을 가지고 있는지 확인합니다.
     *
     * @return 관리자 권한이 있으면 true
     */
    public boolean hasAdminAccess() {
        return this.role != null && this.role.hasAdminAccess();
    }

    /**
     * 멤버가 소유자인지 확인합니다.
     *
     * @return 소유자이면 true
     */
    public boolean isOwner() {
        return this.role == MemberRole.OWNER;
    }

    /**
     * 멤버가 활성 상태(삭제되지 않음)인지 확인합니다.
     *
     * @return 활성 상태이면 true
     */
    public boolean isMemberActive() {
        return !isDeleted();
    }

    // ==================== 상태 변경 메서드 ====================

    /**
     * 이벤트로 받아온 정보로 상태를 업데이트합니다.
     *
     * @param ledgerId  가계부 ID
     * @param accountId 계정 ID
     * @param role      역할
     */
    public void updateFromEvent(Long ledgerId, Long accountId, MemberRole role) {
        this.ledgerId = ledgerId;
        this.accountId = accountId;
        this.role = role;
    }

    /**
     * 멤버의 역할을 변경합니다.
     *
     * @param newRole 새로운 역할
     */
    public void updateRole(MemberRole newRole) {
        this.role = newRole;
    }
}
