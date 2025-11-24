package com.hamkkebu.boilerplate.data.event;

import lombok.*;

/**
 * 거래 수정 이벤트
 *
 * <p>Zero-Payload 패턴: 거래 ID와 사용자 ID만 포함</p>
 * <p>상세 정보가 필요한 경우 Consumer에서 Transaction API를 호출하여 조회</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * TransactionUpdatedEvent event = TransactionUpdatedEvent.builder()
 *     .transactionId("tx-123")
 *     .userId("user-123")
 *     .ledgerId("ledger-123")
 *     .build();
 *
 * eventPublisher.publish("transaction.events", event);
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class TransactionUpdatedEvent extends BaseEvent {

    private static final String EVENT_TYPE = "TRANSACTION_UPDATED";

    /**
     * 가계부 ID (선택적)
     */
    private String ledgerId;

    @Builder
    public TransactionUpdatedEvent(String transactionId, String userId, String ledgerId, String metadata) {
        super(EVENT_TYPE, transactionId, userId, metadata);
        this.ledgerId = ledgerId;
    }

    /**
     * 간단한 생성자
     */
    public TransactionUpdatedEvent(String transactionId, String userId, String ledgerId) {
        super(EVENT_TYPE, transactionId, userId);
        this.ledgerId = ledgerId;
    }
}
