package com.hamkkebu.boilerplate.common.ledger.event;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import lombok.*;

/**
 * 가계부 멤버 역할 변경 이벤트
 *
 * <p>멤버의 역할이 변경되었을 때 발행됩니다.</p>
 * <p>다른 서비스에서 멤버의 역할을 업데이트합니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class LedgerMemberRoleChangedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "LEDGER_MEMBER_ROLE_CHANGED";

    private Long ledgerMemberId;
    private Long ledgerId;
    private Long accountId;
    private String newRole;

    @Override
    public String getResourceId() {
        return String.valueOf(ledgerMemberId);
    }

    @Builder
    public LedgerMemberRoleChangedEvent(Long ledgerMemberId, Long ledgerId, Long accountId, String newRole) {
        super(EVENT_TYPE, String.valueOf(ledgerMemberId), String.valueOf(accountId));
        this.ledgerMemberId = ledgerMemberId;
        this.ledgerId = ledgerId;
        this.accountId = accountId;
        this.newRole = newRole;
    }
}
