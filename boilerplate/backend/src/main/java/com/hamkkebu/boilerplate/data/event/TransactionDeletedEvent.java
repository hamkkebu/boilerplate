package com.hamkkebu.boilerplate.data.event;

import lombok.*;

/**
 * 거래 삭제 이벤트
 *
 * <p>Zero-Payload 패턴: 거래 ID와 사용자 ID만 포함</p>
 * <p>삭제된 데이터는 조회할 수 없으므로, 필요한 경우 metadata에 최소 정보를 포함할 수 있습니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * TransactionDeletedEvent event = TransactionDeletedEvent.builder()
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
public class TransactionDeletedEvent extends BaseEvent {

    private static final String EVENT_TYPE = "TRANSACTION_DELETED";

    /**
     * 가계부 ID (선택적)
     */
    private String ledgerId;

    @Builder
    public TransactionDeletedEvent(String transactionId, String userId, String ledgerId, String metadata) {
        super(EVENT_TYPE, transactionId, userId, metadata);
        this.ledgerId = ledgerId;
    }

    /**
     * 간단한 생성자
     */
    public TransactionDeletedEvent(String transactionId, String userId, String ledgerId) {
        super(EVENT_TYPE, transactionId, userId);
        this.ledgerId = ledgerId;
    }
}
