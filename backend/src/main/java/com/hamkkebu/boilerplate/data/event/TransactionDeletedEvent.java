package com.hamkkebu.boilerplate.data.event;

import lombok.*;

/**
 * 거래 삭제 이벤트 (Zero-Payload)
 *
 * <p>transaction-service에서 거래 삭제 시 발행하는 이벤트입니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class TransactionDeletedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "TRANSACTION_DELETED";

    private String transactionId;
    private String ledgerId;

    @Override
    public String getResourceId() {
        return transactionId;
    }

    @Builder
    public TransactionDeletedEvent(String transactionId, String userId, String ledgerId) {
        super(EVENT_TYPE, transactionId, userId);
        this.transactionId = transactionId;
        this.ledgerId = ledgerId;
    }
}
