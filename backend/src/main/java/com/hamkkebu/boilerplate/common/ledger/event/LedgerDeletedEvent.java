package com.hamkkebu.boilerplate.common.ledger.event;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import lombok.*;

/**
 * 가계부 삭제 이벤트
 *
 * <p>ledger-service에서 발행한 LEDGER_DELETED 이벤트를 수신합니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class LedgerDeletedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "LEDGER_DELETED";

    private Long ledgerId;

    @Override
    public String getResourceId() {
        return String.valueOf(ledgerId);
    }

    @Builder
    public LedgerDeletedEvent(Long ledgerId, Long userId) {
        super(EVENT_TYPE, String.valueOf(ledgerId), String.valueOf(userId));
        this.ledgerId = ledgerId;
    }
}
