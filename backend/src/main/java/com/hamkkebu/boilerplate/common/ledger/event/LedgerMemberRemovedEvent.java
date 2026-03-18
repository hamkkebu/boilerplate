package com.hamkkebu.boilerplate.common.ledger.event;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import lombok.*;

/**
 * 가계부 멤버 제거 이벤트
 *
 * <p>멤버가 가계부에서 제거되었을 때 발행됩니다.</p>
 * <p>다른 서비스에서 해당 멤버를 soft delete 처리합니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class LedgerMemberRemovedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "LEDGER_MEMBER_REMOVED";

    private Long ledgerMemberId;
    private Long ledgerId;
    private Long accountId;

    @Override
    public String getResourceId() {
        return String.valueOf(ledgerMemberId);
    }

    @Builder
    public LedgerMemberRemovedEvent(Long ledgerMemberId, Long ledgerId, Long accountId) {
        super(EVENT_TYPE, String.valueOf(ledgerMemberId), String.valueOf(accountId));
        this.ledgerMemberId = ledgerMemberId;
        this.ledgerId = ledgerId;
        this.accountId = accountId;
    }
}
