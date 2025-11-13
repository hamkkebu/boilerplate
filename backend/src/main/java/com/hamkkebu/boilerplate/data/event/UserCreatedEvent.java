package com.hamkkebu.boilerplate.data.event;

import lombok.*;

/**
 * 사용자 생성 이벤트
 *
 * <p>Zero-Payload 패턴: 사용자 ID만 포함</p>
 * <p>상세 정보가 필요한 경우 Consumer에서 User API를 호출하여 조회</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * UserCreatedEvent event = UserCreatedEvent.builder()
 *     .userId("user-123")
 *     .build();
 *
 * eventPublisher.publish("user.events", event);
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class UserCreatedEvent extends BaseEvent {

    private static final String EVENT_TYPE = "USER_CREATED";

    @Builder
    public UserCreatedEvent(String userId, String metadata) {
        super(EVENT_TYPE, userId, userId, metadata);
    }

    /**
     * 간단한 생성자
     */
    public UserCreatedEvent(String userId) {
        super(EVENT_TYPE, userId, userId);
    }
}
