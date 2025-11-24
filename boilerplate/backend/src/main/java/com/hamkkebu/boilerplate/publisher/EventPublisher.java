package com.hamkkebu.boilerplate.publisher;

import com.hamkkebu.boilerplate.data.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 이벤트 발행 클래스
 * Kafka를 통해 Zero-Payload 이벤트를 발행합니다.
 *
 * <p>사용 예시:</p>
 * <pre>
 * eventPublisher.publish("user.events", userCreatedEvent);
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 이벤트를 지정된 토픽으로 발행 (비동기)
     *
     * @param topic 토픽 이름
     * @param event 발행할 이벤트
     */
    public void publish(String topic, DomainEvent event) {
        log.info("Publishing event to topic [{}]: eventType={}, resourceId={}, eventId={}",
            topic, event.getEventType(), event.getResourceId(), event.getEventId());

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(topic, event.getResourceId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic [{}]: eventId={}, error={}",
                    topic, event.getEventId(), ex.getMessage(), ex);
            } else {
                log.info("Successfully published event to topic [{}]: eventId={}, partition={}, offset={}",
                    topic, event.getEventId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            }
        });
    }

    /**
     * 이벤트를 지정된 토픽으로 발행 (동기)
     * 발행 완료를 기다려야 하는 경우 사용
     *
     * @param topic 토픽 이름
     * @param event 발행할 이벤트
     * @throws RuntimeException 발행 실패 시
     */
    public void publishSync(String topic, DomainEvent event) {
        log.info("Publishing event synchronously to topic [{}]: eventType={}, resourceId={}, eventId={}",
            topic, event.getEventType(), event.getResourceId(), event.getEventId());

        try {
            SendResult<String, Object> result =
                kafkaTemplate.send(topic, event.getResourceId(), event).get();

            log.info("Successfully published event to topic [{}]: eventId={}, partition={}, offset={}",
                topic, event.getEventId(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
        } catch (Exception ex) {
            log.error("Failed to publish event synchronously to topic [{}]: eventId={}, error={}",
                topic, event.getEventId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to publish event", ex);
        }
    }

    /**
     * 여러 이벤트를 한 번에 발행 (비동기)
     *
     * @param topic  토픽 이름
     * @param events 발행할 이벤트 목록
     */
    public void publishBatch(String topic, Iterable<? extends DomainEvent> events) {
        log.info("Publishing batch events to topic [{}]", topic);

        events.forEach(event -> publish(topic, event));
    }
}
