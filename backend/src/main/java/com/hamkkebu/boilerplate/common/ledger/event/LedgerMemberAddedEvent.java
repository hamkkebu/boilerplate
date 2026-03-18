package com.hamkkebu.boilerplate.common.ledger.event;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import lombok.*;

/**
 * 가계부 멤버 추가 이벤트
 *
 * <p>ledger-service에서 가계부에 새로운 멤버가 추가되었을 때 발행됩니다.</p>
 * <p>다른 서비스(transaction-service 등)에서 멤버 정보를 동기화할 수 있도록 합니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class LedgerMemberAddedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "LEDGER_MEMBER_ADDED";

    private Long ledgerMemberId;
    private Long ledgerId;
    private Long accountId;
    private String role;

    @Override
    public String getResourceId() {
        return String.valueOf(ledgerMemberId);
    }

    @Builder
    public LedgerMemberAddedEvent(Long ledgerMemberId, Long ledgerId, Long accountId, String role) {
        super(EVENT_TYPE, String.valueOf(ledgerMemberId), String.valueOf(accountId));
        this.ledgerMemberId = ledgerMemberId;
        this.ledgerId = ledgerId;
        this.accountId = accountId;
        this.role = role;
    }
}
