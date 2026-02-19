package com.hamkkebu.boilerplate.data.event;

import lombok.*;

/**
 * 거래 생성 이벤트 (Zero-Payload)
 *
 * <p>transaction-service에서 거래 생성 시 발행하는 이벤트입니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class TransactionCreatedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "TRANSACTION_CREATED";

    private String transactionId;
    private String ledgerId;

    @Override
    public String getResourceId() {
        return transactionId;
    }

    @Builder
    public TransactionCreatedEvent(String transactionId, String userId, String ledgerId) {
        super(EVENT_TYPE, transactionId, userId);
        this.transactionId = transactionId;
        this.ledgerId = ledgerId;
    }
}
