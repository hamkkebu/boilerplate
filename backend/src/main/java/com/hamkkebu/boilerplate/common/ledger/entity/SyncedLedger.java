package com.hamkkebu.boilerplate.common.ledger.entity;

import com.hamkkebu.boilerplate.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 동기화된 가계부 엔티티 (Ledger Service에서 동기화)
 *
 * <p>ledger-service에서 Kafka 이벤트를 통해 동기화된 가계부 정보를 저장합니다.</p>
 * <p>각 서비스(transaction-service 등)에서 상속받아 사용합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Entity
 * @Table(name = "tbl_ledgers")
 * public class Ledger extends SyncedLedger {
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
public abstract class SyncedLedger extends BaseEntity {

    @Id
    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "currency", nullable = false, length = 10)
    @Builder.Default
    private String currency = "KRW";

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * 가계부 정보 업데이트 (이벤트로 받아온 정보로 갱신)
     *
     * @param name        가계부 이름
     * @param description 설명
     * @param currency    통화
     * @param isDefault   기본 가계부 여부
     */
    public void updateFromLedgerService(String name, String description, String currency, Boolean isDefault) {
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.isDefault = isDefault;
    }

    /**
     * 가계부가 특정 사용자의 소유인지 확인
     *
     * @param userId 사용자 ID
     * @return 해당 사용자 소유이면 true
     */
    public boolean isOwnedBy(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    /**
     * 가계부가 활성 상태인지 확인
     *
     * @return 삭제되지 않았으면 true
     */
    public boolean isLedgerActive() {
        return !isDeleted();
    }
}
