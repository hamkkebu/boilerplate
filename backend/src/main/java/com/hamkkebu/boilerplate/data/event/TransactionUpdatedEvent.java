package com.hamkkebu.boilerplate.data.event;

import lombok.*;

/**
 * 거래 수정 이벤트 (Zero-Payload)
 *
 * <p>transaction-service에서 거래 수정 시 발행하는 이벤트입니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class TransactionUpdatedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "TRANSACTION_UPDATED";

    private String transactionId;
    private String ledgerId;

    @Override
    public String getResourceId() {
        return transactionId;
    }

    @Builder
    public TransactionUpdatedEvent(String transactionId, String userId, String ledgerId) {
        super(EVENT_TYPE, transactionId, userId);
        this.transactionId = transactionId;
        this.ledgerId = ledgerId;
    }
}
