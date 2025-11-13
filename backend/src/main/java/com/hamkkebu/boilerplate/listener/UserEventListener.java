package com.hamkkebu.boilerplate.listener;

import com.hamkkebu.boilerplate.data.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 사용자 이벤트 리스너
 *
 * <p>user.events 토픽에서 사용자 관련 이벤트를 수신합니다.</p>
 *
 * <p>Zero-Payload 패턴:</p>
 * <ul>
 *   <li>이벤트에서 사용자 ID를 추출</li>
 *   <li>필요한 경우 User API를 호출하여 상세 정보 조회</li>
 *   <li>비즈니스 로직 수행</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    // 필요한 경우 외부 API 클라이언트나 서비스를 주입
    // private final UserServiceClient userServiceClient;

    /**
     * 사용자 생성 이벤트 처리
     *
     * @param event 사용자 생성 이벤트
     */
    @KafkaListener(
        topics = "user.events",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
        filter = "userCreatedEventFilter"
    )
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent: eventId={}, userId={}, occurredAt={}",
            event.getEventId(), event.getUserId(), event.getOccurredAt());

        try {
            // Zero-Payload 패턴: 필요한 경우 User API를 호출하여 상세 정보 조회
            // User user = userServiceClient.getUser(event.getUserId());

            // 비즈니스 로직 수행
            // 예: 환영 이메일 발송, 초기 설정 등
            processUserCreation(event.getUserId());

            log.info("Successfully processed UserCreatedEvent: userId={}", event.getUserId());
        } catch (Exception ex) {
            log.error("Failed to process UserCreatedEvent: eventId={}, userId={}, error={}",
                event.getEventId(), event.getUserId(), ex.getMessage(), ex);
            // 에러 처리: DLQ로 전송, 재시도 등
        }
    }

    private void processUserCreation(String userId) {
        // 실제 비즈니스 로직 구현
        log.info("Processing user creation: userId={}", userId);
        // 예: 환영 이메일 발송, 기본 가계부 생성 등
    }
}
