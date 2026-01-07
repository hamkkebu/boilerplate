package com.hamkkebu.boilerplate.common.ledger.event;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import lombok.*;

/**
 * 가계부 생성 이벤트
 *
 * <p>ledger-service에서 발행한 LEDGER_CREATED 이벤트를 수신합니다.</p>
 * <p>가계부 정보 전체를 포함하여 다른 서비스에서 동기화할 수 있도록 합니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class LedgerCreatedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "LEDGER_CREATED";

    private Long ledgerId;
    private Long userId;
    private String name;
    private String description;
    private String currency;
    private Boolean isDefault;

    @Override
    public String getResourceId() {
        return String.valueOf(ledgerId);
    }

    @Builder
    public LedgerCreatedEvent(Long ledgerId, Long userId, String name, String description,
                               String currency, Boolean isDefault) {
        super(EVENT_TYPE, String.valueOf(ledgerId), String.valueOf(userId));
        this.ledgerId = ledgerId;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.isDefault = isDefault;
    }
}
