package com.hamkkebu.boilerplate.listener;

import com.hamkkebu.boilerplate.common.kafka.IdempotencyService;
import com.hamkkebu.boilerplate.data.event.TransactionCreatedEvent;
import com.hamkkebu.boilerplate.data.event.TransactionDeletedEvent;
import com.hamkkebu.boilerplate.data.event.TransactionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 거래 이벤트 리스너
 *
 * <p>transaction.events 토픽에서 거래 관련 이벤트를 수신합니다.</p>
 *
 * <p>Zero-Payload 패턴:</p>
 * <ul>
 *   <li>이벤트에서 거래 ID를 추출</li>
 *   <li>필요한 경우 Transaction API를 호출하여 상세 정보 조회</li>
 *   <li>비즈니스 로직 수행 (통계 업데이트, 알림 발송 등)</li>
 * </ul>
 *
 * <p>에러 처리 (KafkaConfig의 DefaultErrorHandler):</p>
 * <ul>
 *   <li>재시도: Exponential Backoff (1초, 2초, 4초 - 총 3회)</li>
 *   <li>3회 재시도 후 실패 시: DLQ (transaction.events.DLQ)로 자동 전송</li>
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
public class TransactionEventListener {

    private final IdempotencyService idempotencyService;

    private static final String TOPIC = "transaction.events";

    // 필요한 경우 외부 API 클라이언트나 서비스를 주입
    // private final TransactionServiceClient transactionServiceClient;
    // private final AnalyticsService analyticsService;
    // private final NotificationService notificationService;

    /**
     * 거래 생성 이벤트 처리 (Manual ACK)
     *
     * @param event 거래 생성 이벤트
     * @param ack Kafka offset commit을 위한 Acknowledgment
     */
    @KafkaListener(
        topics = TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
        filter = "transactionCreatedEventFilter"
    )
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event, Acknowledgment ack) {
        log.info("Received TransactionCreatedEvent: eventId={}, transactionId={}, ledgerId={}, userId={}",
            event.getEventId(), event.getResourceId(), event.getLedgerId(), event.getUserId());

        try {
            // IDEMPOTENCY: Atomic check-and-set (RACE CONDITION SAFE)
            if (!idempotencyService.tryMarkAsProcessed(TOPIC, event.getEventId())) {
                log.info("Skipping duplicate TransactionCreatedEvent (atomic): eventId={}, transactionId={}",
                    event.getEventId(), event.getResourceId());
                ack.acknowledge();
                return;
            }

            // Zero-Payload 패턴: 필요한 경우 Transaction API를 호출하여 상세 정보 조회
            // Transaction transaction = transactionServiceClient.getTransaction(event.getResourceId());

            // 비즈니스 로직 수행
            processTransactionCreation(event.getResourceId(), event.getLedgerId(), event.getUserId());

            // MANUAL ACK: 처리 성공 시 offset 커밋
            ack.acknowledge();

            log.info("Successfully processed TransactionCreatedEvent: transactionId={}", event.getResourceId());

        } catch (Exception e) {
            log.error("Failed to process TransactionCreatedEvent: eventId={}, transactionId={}, error={}",
                event.getEventId(), event.getResourceId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 거래 수정 이벤트 처리 (Manual ACK)
     *
     * @param event 거래 수정 이벤트
     * @param ack Kafka offset commit을 위한 Acknowledgment
     */
    @KafkaListener(
        topics = TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
        filter = "transactionUpdatedEventFilter"
    )
    public void handleTransactionUpdatedEvent(TransactionUpdatedEvent event, Acknowledgment ack) {
        log.info("Received TransactionUpdatedEvent: eventId={}, transactionId={}, ledgerId={}, userId={}",
            event.getEventId(), event.getResourceId(), event.getLedgerId(), event.getUserId());

        try {
            // IDEMPOTENCY: Atomic check-and-set (RACE CONDITION SAFE)
            if (!idempotencyService.tryMarkAsProcessed(TOPIC, event.getEventId())) {
                log.info("Skipping duplicate TransactionUpdatedEvent (atomic): eventId={}, transactionId={}",
                    event.getEventId(), event.getResourceId());
                ack.acknowledge();
                return;
            }

            // Transaction transaction = transactionServiceClient.getTransaction(event.getResourceId());
            processTransactionUpdate(event.getResourceId(), event.getLedgerId(), event.getUserId());

            // MANUAL ACK: 처리 성공 시 offset 커밋
            ack.acknowledge();

            log.info("Successfully processed TransactionUpdatedEvent: transactionId={}", event.getResourceId());

        } catch (Exception e) {
            log.error("Failed to process TransactionUpdatedEvent: eventId={}, transactionId={}, error={}",
                event.getEventId(), event.getResourceId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 거래 삭제 이벤트 처리 (Manual ACK)
     *
     * @param event 거래 삭제 이벤트
     * @param ack Kafka offset commit을 위한 Acknowledgment
     */
    @KafkaListener(
        topics = TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
        filter = "transactionDeletedEventFilter"
    )
    public void handleTransactionDeletedEvent(TransactionDeletedEvent event, Acknowledgment ack) {
        log.info("Received TransactionDeletedEvent: eventId={}, transactionId={}, ledgerId={}, userId={}",
            event.getEventId(), event.getResourceId(), event.getLedgerId(), event.getUserId());

        try {
            // IDEMPOTENCY: Atomic check-and-set (RACE CONDITION SAFE)
            if (!idempotencyService.tryMarkAsProcessed(TOPIC, event.getEventId())) {
                log.info("Skipping duplicate TransactionDeletedEvent (atomic): eventId={}, transactionId={}",
                    event.getEventId(), event.getResourceId());
                ack.acknowledge();
                return;
            }

            // 삭제된 데이터는 조회할 수 없으므로 이벤트 정보만으로 처리
            processTransactionDeletion(event.getResourceId(), event.getLedgerId(), event.getUserId());

            // MANUAL ACK: 처리 성공 시 offset 커밋
            ack.acknowledge();

            log.info("Successfully processed TransactionDeletedEvent: transactionId={}", event.getResourceId());

        } catch (Exception e) {
            log.error("Failed to process TransactionDeletedEvent: eventId={}, transactionId={}, error={}",
                event.getEventId(), event.getResourceId(), e.getMessage(), e);
            throw e;
        }
    }

    private void processTransactionCreation(String transactionId, String ledgerId, String userId) {
        // 실제 비즈니스 로직 구현
        log.info("Processing transaction creation: transactionId={}, ledgerId={}, userId={}",
            transactionId, ledgerId, userId);
        // 예: 통계 업데이트, 예산 초과 알림 등
    }

    private void processTransactionUpdate(String transactionId, String ledgerId, String userId) {
        log.info("Processing transaction update: transactionId={}, ledgerId={}, userId={}",
            transactionId, ledgerId, userId);
        // 예: 통계 재계산, 캐시 무효화 등
    }

    private void processTransactionDeletion(String transactionId, String ledgerId, String userId) {
        log.info("Processing transaction deletion: transactionId={}, ledgerId={}, userId={}",
            transactionId, ledgerId, userId);
        // 예: 통계 업데이트, 관련 데이터 정리 등
    }
}
