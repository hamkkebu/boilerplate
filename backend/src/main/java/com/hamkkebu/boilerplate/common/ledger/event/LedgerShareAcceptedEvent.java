package com.hamkkebu.boilerplate.common.ledger.event;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import lombok.*;

/**
 * 가계부 공유 수락 이벤트
 *
 * <p>공유 수신자가 공유 요청을 수락했을 때 발행됩니다.</p>
 * <p>다른 서비스에서 공유 상태를 ACCEPTED로 업데이트합니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class LedgerShareAcceptedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "LEDGER_SHARE_ACCEPTED";

    private Long ledgerShareId;
    private Long ledgerId;
    private Long sharedUserId;

    @Override
    public String getResourceId() {
        return String.valueOf(ledgerShareId);
    }

    @Builder
    public LedgerShareAcceptedEvent(Long ledgerShareId, Long ledgerId, Long sharedUserId) {
        super(EVENT_TYPE, String.valueOf(ledgerShareId), String.valueOf(sharedUserId));
        this.ledgerShareId = ledgerShareId;
        this.ledgerId = ledgerId;
        this.sharedUserId = sharedUserId;
    }
}
