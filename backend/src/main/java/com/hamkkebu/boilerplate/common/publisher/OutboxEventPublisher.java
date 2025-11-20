package com.hamkkebu.boilerplate.common.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hamkkebu.boilerplate.data.entity.OutboxEvent;
import com.hamkkebu.boilerplate.data.event.DomainEvent;
import com.hamkkebu.boilerplate.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox Event Publisher
 *
 * <p>Transactional Outbox 패턴을 사용하여 이벤트를 발행합니다.</p>
 *
 * <p>동작 방식:</p>
 * <ol>
 *   <li>비즈니스 로직과 같은 트랜잭션 내에서 이벤트를 Outbox 테이블에 저장</li>
 *   <li>트랜잭션 커밋되면 이벤트도 함께 저장됨 (원자성 보장)</li>
 *   <li>별도의 Scheduler가 Outbox 테이블을 주기적으로 조회하여 Kafka로 발행</li>
 * </ol>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Transactional
 * public void createUser(User user) {
 *     userRepository.save(user);
 *     outboxEventPublisher.publish("user.events", userCreatedEvent);
 *     // 트랜잭션 커밋 시 user와 event가 모두 저장됨
 * }
 * }
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * 이벤트를 Outbox 테이블에 저장
     *
     * <p>MANDATORY 전파 속성: 반드시 기존 트랜잭션 내에서 실행되어야 함</p>
     * <p>비즈니스 로직과 함께 커밋되어 원자성 보장</p>
     *
     * @param topic Kafka 토픽명
     * @param event 발행할 이벤트
     * @throws RuntimeException 기존 트랜잭션이 없는 경우
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String topic, DomainEvent event) {
        try {
            // 이벤트 객체를 JSON으로 직렬화
            String payload = objectMapper.writeValueAsString(event);

            // Outbox 테이블에 저장
            OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .topic(topic)
                .resourceId(event.getResourceId())
                .payload(payload)
                .build();

            outboxEventRepository.save(outboxEvent);

            log.info("Saved event to outbox: eventId={}, eventType={}, topic={}",
                event.getEventId(), event.getEventType(), topic);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event to JSON: eventId={}, eventType={}",
                event.getEventId(), event.getEventType(), e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    /**
     * 여러 이벤트를 Outbox 테이블에 일괄 저장
     *
     * @param topic  Kafka 토픽명
     * @param events 발행할 이벤트 목록
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishBatch(String topic, Iterable<? extends DomainEvent> events) {
        log.info("Saving batch events to outbox: topic={}", topic);
        events.forEach(event -> publish(topic, event));
    }
}
