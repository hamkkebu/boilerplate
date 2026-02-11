package com.hamkkebu.boilerplate.common.ledger.entity;

import com.hamkkebu.boilerplate.common.entity.BaseEntity;
import com.hamkkebu.boilerplate.common.enums.SharePermission;
import com.hamkkebu.boilerplate.common.enums.ShareStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 동기화된 가계부 공유 엔티티 (Ledger Service에서 동기화)
 *
 * <p>ledger-service에서 Kafka 이벤트를 통해 동기화된 가계부 공유 정보를 저장합니다.</p>
 * <p>각 서비스(transaction-service 등)에서 상속받아 사용합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Entity
 * @Table(name = "tbl_ledger_shares")
 * public class LedgerShare extends SyncedLedgerShare {
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
public abstract class SyncedLedgerShare extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_share_id", nullable = false)
    private Long ledgerShareId;

    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "shared_user_id", nullable = false)
    private Long sharedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ShareStatus status = ShareStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 20)
    @Builder.Default
    private SharePermission permission = SharePermission.READ_ONLY;

    @Column(name = "shared_at", nullable = false)
    @Builder.Default
    private LocalDateTime sharedAt = LocalDateTime.now();

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // ==================== 상태 변경 메서드 ====================

    /**
     * 공유 요청을 수락합니다.
     *
     * <p>상태를 ACCEPTED로 변경하고, 수락 시각을 기록합니다.</p>
     *
     * @throws IllegalStateException PENDING 상태가 아닌 경우
     */
    public void accept() {
        if (!isPending()) {
            throw new IllegalStateException("PENDING 상태에서만 수락할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = ShareStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    /**
     * 공유 요청을 거절합니다.
     *
     * <p>상태를 REJECTED로 변경하고, 거절 사유를 기록합니다.</p>
     *
     * @param reason 거절 사유 (nullable)
     * @throws IllegalStateException PENDING 상태가 아닌 경우
     */
    public void reject(String reason) {
        if (!isPending()) {
            throw new IllegalStateException("PENDING 상태에서만 거절할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = ShareStatus.REJECTED;
        this.rejectionReason = reason;
    }

    // ==================== 상태 확인 메서드 ====================

    /**
     * 공유가 수락된 상태인지 확인합니다.
     *
     * @return ACCEPTED 상태이면 true
     */
    public boolean isAccepted() {
        return ShareStatus.ACCEPTED.equals(this.status);
    }

    /**
     * 공유가 대기 중인 상태인지 확인합니다.
     *
     * @return PENDING 상태이면 true
     */
    public boolean isPending() {
        return ShareStatus.PENDING.equals(this.status);
    }

    /**
     * 공유가 거절된 상태인지 확인합니다.
     *
     * @return REJECTED 상태이면 true
     */
    public boolean isRejected() {
        return ShareStatus.REJECTED.equals(this.status);
    }

    /**
     * 공유가 활성 상태(수락됨 + 삭제되지 않음)인지 확인합니다.
     *
     * @return 수락되었고 삭제되지 않았으면 true
     */
    public boolean isShareActive() {
        return isAccepted() && !isDeleted();
    }

    /**
     * 특정 사용자가 이 공유의 수신자인지 확인합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자가 공유 수신자이면 true
     */
    public boolean isSharedWith(Long userId) {
        return this.sharedUserId != null && this.sharedUserId.equals(userId);
    }

    /**
     * 특정 사용자가 이 공유의 소유자인지 확인합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자가 공유 소유자이면 true
     */
    public boolean isOwnedBy(Long userId) {
        return this.ownerId != null && this.ownerId.equals(userId);
    }

    /**
     * 이벤트로 받아온 정보로 상태를 업데이트합니다.
     *
     * @param status     공유 상태
     * @param permission 공유 권한
     * @param acceptedAt 수락 시각
     */
    public void updateFromEvent(ShareStatus status, SharePermission permission, LocalDateTime acceptedAt) {
        this.status = status;
        this.permission = permission;
        this.acceptedAt = acceptedAt;
    }
}
