package com.hamkkebu.boilerplate.data.event;

import lombok.*;

/**
 * 가계부 생성 이벤트
 *
 * <p>Zero-Payload 패턴: 가계부 ID와 소유자 ID만 포함</p>
 * <p>상세 정보가 필요한 경우 Consumer에서 Ledger API를 호출하여 조회</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * LedgerCreatedEvent event = LedgerCreatedEvent.builder()
 *     .ledgerId("ledger-123")
 *     .userId("user-123")
 *     .build();
 *
 * eventPublisher.publish("ledger.events", event);
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class LedgerCreatedEvent extends BaseEvent {

    private static final String EVENT_TYPE = "LEDGER_CREATED";

    @Builder
    public LedgerCreatedEvent(String ledgerId, String userId, String metadata) {
        super(EVENT_TYPE, ledgerId, userId, metadata);
    }

    /**
     * 간단한 생성자
     */
    public LedgerCreatedEvent(String ledgerId, String userId) {
        super(EVENT_TYPE, ledgerId, userId);
    }
}
