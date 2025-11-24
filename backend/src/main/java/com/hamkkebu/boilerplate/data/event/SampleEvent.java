package com.hamkkebu.boilerplate.data.event;

import lombok.*;

/**
 * 샘플 이벤트
 *
 * <p>Zero-Payload 패턴: 샘플 ID만 포함</p>
 * <p>상세 정보가 필요한 경우 Consumer에서 Sample API를 호출하여 조회</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * SampleEvent event = SampleEvent.builder()
 *     .userId("user-123")
 *     .sampleId("sample-456")
 *     .build();
 *
 * eventPublisher.publish("sample.events", event);
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class SampleEvent extends BaseEvent {

    private static final String EVENT_TYPE = "SAMPLE_CREATED";

    /**
     * 샘플 ID (리소스 ID)
     */
    private String sampleId;

    /**
     * Builder 생성자
     */
    @Builder
    public SampleEvent(String userId, String sampleId, String metadata) {
        super(EVENT_TYPE, sampleId, userId, metadata);
        this.sampleId = sampleId;
    }

    /**
     * 간단한 생성자
     */
    public SampleEvent(String userId, String sampleId) {
        super(EVENT_TYPE, sampleId, userId);
        this.sampleId = sampleId;
    }
}
