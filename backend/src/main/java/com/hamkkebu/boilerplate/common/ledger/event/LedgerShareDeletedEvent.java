package com.hamkkebu.boilerplate.common.ledger.event;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import lombok.*;

/**
 * 가계부 공유 삭제 이벤트
 *
 * <p>소유자 또는 수신자가 공유를 해제했을 때 발행됩니다.</p>
 * <p>다른 서비스에서 해당 공유를 soft delete 처리합니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class LedgerShareDeletedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "LEDGER_SHARE_DELETED";

    private Long ledgerShareId;
    private Long ledgerId;

    @Override
    public String getResourceId() {
        return String.valueOf(ledgerShareId);
    }

    @Builder
    public LedgerShareDeletedEvent(Long ledgerShareId, Long ledgerId, Long userId) {
        super(EVENT_TYPE, String.valueOf(ledgerShareId), String.valueOf(userId));
        this.ledgerShareId = ledgerShareId;
        this.ledgerId = ledgerId;
    }
}
