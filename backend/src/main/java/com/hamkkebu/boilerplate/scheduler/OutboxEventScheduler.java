package com.hamkkebu.boilerplate.scheduler;

import com.hamkkebu.boilerplate.common.enums.OutboxEventStatus;
import com.hamkkebu.boilerplate.data.entity.OutboxEvent;
import com.hamkkebu.boilerplate.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Outbox Event Scheduler
 *
 * <p>Transactional Outbox 패턴의 핵심 컴포넌트</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>주기적으로 Outbox 테이블에서 PENDING 상태의 이벤트 조회</li>
 *   <li>조회된 이벤트를 Kafka로 발행</li>
 *   <li>발행 성공 시 PUBLISHED 상태로 변경</li>
 *   <li>발행 실패 시 재시도 (최대 3회)</li>
 *   <li>재시도 횟수 초과 시 FAILED 상태로 변경</li>
 *   <li>오래된 PUBLISHED 이벤트 삭제 (7일 이상)</li>
 * </ul>
 *
 * <p>스케줄링:</p>
 * <ul>
 *   <li>이벤트 발행: 5초마다 실행</li>
 *   <li>오래된 이벤트 정리: 매일 새벽 3시 실행</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Outbox 이벤트를 Kafka로 발행
     *
     * <p>5초마다 실행되어 PENDING 상태의 이벤트를 처리</p>
     */
    @Scheduled(fixedDelay = 5000) // 5초마다 실행
    @Transactional
    public void publishPendingEvents() {
        // PENDING 상태의 이벤트 조회
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
            OutboxEventStatus.PENDING
        );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending events to publish", pendingEvents.size());

        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                // Kafka로 발행
                publishToKafka(outboxEvent);

            } catch (Exception e) {
                log.error("Failed to publish event to Kafka: eventId={}, error={}",
                    outboxEvent.getEventId(), e.getMessage(), e);

                // 재시도 가능 여부 확인
                if (outboxEvent.canRetry()) {
                    outboxEvent.incrementRetry();
                    outboxEventRepository.save(outboxEvent);
                    log.warn("Incremented retry count for event: eventId={}, retryCount={}/{}",
                        outboxEvent.getEventId(), outboxEvent.getRetryCount(), outboxEvent.getMaxRetry());
                } else {
                    // 최대 재시도 횟수 초과
                    outboxEvent.markAsFailed(e.getMessage());
                    outboxEventRepository.save(outboxEvent);
                    log.error("Event marked as FAILED after {} retries: eventId={}",
                        outboxEvent.getMaxRetry(), outboxEvent.getEventId());
                }
            }
        }
    }

    /**
     * Kafka로 이벤트 발행
     *
     * @param outboxEvent Outbox 이벤트
     * @throws Exception 발행 실패 시
     */
    private void publishToKafka(OutboxEvent outboxEvent) throws Exception {
        log.info("Publishing event to Kafka: eventId={}, topic={}, resourceId={}",
            outboxEvent.getEventId(), outboxEvent.getTopic(), outboxEvent.getResourceId());

        // Kafka로 발행 (동기 방식으로 성공 여부 확인)
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
            outboxEvent.getTopic(),
            outboxEvent.getResourceId(),
            outboxEvent.getPayload() // JSON 문자열을 그대로 전송
        );

        // 발행 결과 대기
        SendResult<String, Object> result = future.get();

        // 발행 성공 처리
        outboxEvent.markAsPublished();
        outboxEventRepository.save(outboxEvent);

        log.info("Successfully published event to Kafka: eventId={}, partition={}, offset={}",
            outboxEvent.getEventId(),
            result.getRecordMetadata().partition(),
            result.getRecordMetadata().offset());
    }

    /**
     * 오래된 PUBLISHED 이벤트 삭제
     *
     * <p>매일 새벽 3시에 실행되어 7일 이상 지난 PUBLISHED 이벤트를 삭제</p>
     */
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    @Transactional
    public void cleanupOldPublishedEvents() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        int deletedCount = outboxEventRepository.deleteByStatusAndPublishedAtBefore(
            OutboxEventStatus.PUBLISHED,
            sevenDaysAgo
        );

        if (deletedCount > 0) {
            log.info("Cleaned up {} old published events (older than 7 days)", deletedCount);
        }
    }

    /**
     * Outbox 이벤트 상태 모니터링 (선택적)
     *
     * <p>매 1분마다 실행되어 각 상태의 이벤트 개수를 로깅</p>
     */
    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    public void monitorOutboxEvents() {
        long pendingCount = outboxEventRepository.countByStatus(OutboxEventStatus.PENDING);
        long publishedCount = outboxEventRepository.countByStatus(OutboxEventStatus.PUBLISHED);
        long failedCount = outboxEventRepository.countByStatus(OutboxEventStatus.FAILED);

        if (pendingCount > 0 || failedCount > 0) {
            log.info("Outbox Event Status - PENDING: {}, PUBLISHED: {}, FAILED: {}",
                pendingCount, publishedCount, failedCount);
        }

        // FAILED 이벤트가 많으면 경고
        if (failedCount > 100) {
            log.warn("WARNING: Too many FAILED events ({}). Please check Kafka connectivity.", failedCount);
        }
    }
}
