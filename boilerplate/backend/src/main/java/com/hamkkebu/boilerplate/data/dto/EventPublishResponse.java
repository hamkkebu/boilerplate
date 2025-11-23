package com.hamkkebu.boilerplate.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이벤트 발행 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventPublishResponse {

    /**
     * 이벤트 ID
     */
    private String eventId;

    /**
     * 대상 ID (userId, ledgerId, transactionId 등)
     */
    private String targetId;

    /**
     * 이벤트 토픽
     */
    private String topic;

    /**
     * 발행 방식 (async/sync)
     */
    private String publishMode;

    public EventPublishResponse(String eventId, String targetId, String topic) {
        this.eventId = eventId;
        this.targetId = targetId;
        this.topic = topic;
        this.publishMode = "async";
    }
}
