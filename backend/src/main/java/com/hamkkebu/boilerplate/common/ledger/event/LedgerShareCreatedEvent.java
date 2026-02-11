package com.hamkkebu.boilerplate.common.ledger.event;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import lombok.*;

/**
 * 가계부 공유 생성 이벤트
 *
 * <p>ledger-service에서 가계부 공유 요청이 생성되었을 때 발행됩니다.</p>
 * <p>다른 서비스(transaction-service 등)에서 공유 정보를 동기화할 수 있도록 합니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class LedgerShareCreatedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "LEDGER_SHARE_CREATED";

    private Long ledgerShareId;
    private Long ledgerId;
    private Long ownerId;
    private Long sharedUserId;
    private String permission;

    @Override
    public String getResourceId() {
        return String.valueOf(ledgerShareId);
    }

    @Builder
    public LedgerShareCreatedEvent(Long ledgerShareId, Long ledgerId, Long ownerId,
                                    Long sharedUserId, String permission) {
        super(EVENT_TYPE, String.valueOf(ledgerShareId), String.valueOf(ownerId));
        this.ledgerShareId = ledgerShareId;
        this.ledgerId = ledgerId;
        this.ownerId = ownerId;
        this.sharedUserId = sharedUserId;
        this.permission = permission;
    }
}
