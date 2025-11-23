package com.hamkkebu.boilerplate.listener;

import com.hamkkebu.boilerplate.common.kafka.IdempotencyService;
import com.hamkkebu.boilerplate.data.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
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
 *
 * <p>에러 처리 (KafkaConfig의 DefaultErrorHandler):</p>
 * <ul>
 *   <li>재시도: Exponential Backoff (1초, 2초, 4초 - 총 3회)</li>
 *   <li>3회 재시도 후 실패 시: DLQ (user.events.DLQ)로 자동 전송</li>
 * </ul>
 *
 * <p>Idempotency (멱등성 보장):</p>
 * <ul>
 *   <li>IdempotencyService를 사용하여 중복 이벤트 처리 방지</li>
 *   <li>이미 처리된 eventId는 Redis에 저장 (TTL: 7일)</li>
 *   <li>중복 이벤트는 처리하지 않고 로그만 남김</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final IdempotencyService idempotencyService;

    private static final String TOPIC = "user.events";

    // 필요한 경우 외부 API 클라이언트나 서비스를 주입
    // private final UserServiceClient userServiceClient;

    /**
     * 사용자 생성 이벤트 처리 (Manual ACK)
     *
     * @param event 사용자 생성 이벤트
     * @param ack Kafka offset commit을 위한 Acknowledgment
     */
    @KafkaListener(
        topics = TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
        filter = "userCreatedEventFilter"
    )
    public void handleUserCreatedEvent(UserCreatedEvent event, Acknowledgment ack) {
        log.info("Received UserCreatedEvent: eventId={}, userId={}, occurredAt={}",
            event.getEventId(), event.getUserId(), event.getOccurredAt());

        try {
            // IDEMPOTENCY: Atomic check-and-set (RACE CONDITION SAFE)
            // Redis SETNX로 중복 체크와 마킹을 동시에 수행
            if (!idempotencyService.tryMarkAsProcessed(TOPIC, event.getEventId())) {
                log.info("Skipping duplicate UserCreatedEvent (atomic check): eventId={}, userId={}",
                    event.getEventId(), event.getUserId());
                // 중복 이벤트도 성공으로 처리 (이미 처리됨)
                ack.acknowledge();
                return;
            }

            // Zero-Payload 패턴: 필요한 경우 User API를 호출하여 상세 정보 조회
            // User user = userServiceClient.getUser(event.getUserId());

            // 비즈니스 로직 수행
            // 예: 환영 이메일 발송, 초기 설정 등
            processUserCreation(event.getUserId());

            // MANUAL ACK: 처리 성공 시 offset 커밋
            ack.acknowledge();

            log.info("Successfully processed UserCreatedEvent: userId={}", event.getUserId());

        } catch (Exception e) {
            // 예외 발생 시 ACK하지 않음 → offset 커밋 안 됨
            // DefaultErrorHandler가 재시도 및 DLQ 처리
            log.error("Failed to process UserCreatedEvent: eventId={}, userId={}, error={}",
                event.getEventId(), event.getUserId(), e.getMessage(), e);
            throw e;  // 재시도를 위해 예외 재발생
        }
    }

    private void processUserCreation(String userId) {
        // 실제 비즈니스 로직 구현
        log.info("Processing user creation: userId={}", userId);
        // 예: 환영 이메일 발송, 기본 가계부 생성 등
    }
}
