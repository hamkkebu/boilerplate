package com.hamkkebu.boilerplate.common.ledger.consumer;

import com.hamkkebu.boilerplate.common.ledger.entity.SyncedLedger;
import com.hamkkebu.boilerplate.common.ledger.event.LedgerCreatedEvent;
import com.hamkkebu.boilerplate.common.ledger.event.LedgerDeletedEvent;
import com.hamkkebu.boilerplate.common.ledger.event.LedgerUpdatedEvent;
import com.hamkkebu.boilerplate.common.ledger.repository.SyncedLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 가계부 이벤트 Kafka Consumer 추상 클래스
 *
 * <p>ledger-service에서 발행한 가계부 관련 이벤트를 수신합니다.</p>
 * <ul>
 *   <li>LEDGER_CREATED: 신규 가계부 동기화</li>
 *   <li>LEDGER_UPDATED: 가계부 정보 갱신</li>
 *   <li>LEDGER_DELETED: 가계부 삭제 (soft delete)</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Slf4j
 * @Component
 * public class LedgerEventConsumer extends AbstractLedgerEventConsumer<Ledger> {
 *
 *     public LedgerEventConsumer(LedgerRepository ledgerRepository) {
 *         super(ledgerRepository);
 *     }
 *
 *     @Override
 *     protected Ledger createLedgerEntity(Map<String, Object> eventData) {
 *         return Ledger.builder()
 *             .ledgerId(extractLedgerId(eventData))
 *             .userId(extractUserId(eventData))
 *             .name(extractString(eventData, "name"))
 *             .description(extractString(eventData, "description"))
 *             .currency(extractString(eventData, "currency"))
 *             .isDefault(extractBoolean(eventData, "isDefault"))
 *             .build();
 *     }
 *
 *     @KafkaListener(
 *             topics = "${kafka.topics.ledger-events:ledger.events}",
 *             groupId = "transaction-service-group",
 *             containerFactory = "transactionKafkaListenerContainerFactory"
 *     )
 *     @Transactional
 *     public void handleLedgerEvent(Map<String, Object> eventData) {
 *         processLedgerEvent(eventData);
 *     }
 * }
 * }
 * </pre>
 *
 * @param <T> SyncedLedger를 상속받은 엔티티 타입
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLedgerEventConsumer<T extends SyncedLedger> {

    private final SyncedLedgerRepository<T> ledgerRepository;

    /**
     * 가계부 이벤트 처리 (서브클래스에서 KafkaListener와 함께 호출)
     *
     * @param eventData 이벤트 데이터 맵
     */
    @Transactional
    protected void processLedgerEvent(Map<String, Object> eventData) {
        String eventType = (String) eventData.get("eventType");
        String eventId = (String) eventData.get("eventId");

        log.info("[Kafka Consumer] Received ledger event: eventType={}, eventId={}", eventType, eventId);

        try {
            if (LedgerCreatedEvent.EVENT_TYPE.equals(eventType)) {
                handleLedgerCreated(eventData);
            } else if (LedgerUpdatedEvent.EVENT_TYPE.equals(eventType)) {
                handleLedgerUpdated(eventData);
            } else if (LedgerDeletedEvent.EVENT_TYPE.equals(eventType)) {
                handleLedgerDeleted(eventData);
            } else {
                log.warn("[Kafka Consumer] Unknown ledger event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("[Kafka Consumer] Failed to process ledger event: eventType={}, eventId={}, error={}",
                    eventType, eventId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * LEDGER_CREATED 이벤트 처리
     */
    private void handleLedgerCreated(Map<String, Object> eventData) {
        Long ledgerId = extractLedgerId(eventData);
        log.info("[Kafka Consumer] Processing LEDGER_CREATED: ledgerId={}", ledgerId);

        // 이미 존재하는 가계부인지 확인
        if (ledgerRepository.existsByLedgerIdAndIsDeletedFalse(ledgerId)) {
            log.info("[Kafka Consumer] Ledger already exists: ledgerId={}", ledgerId);
            return;
        }

        // 가계부 엔티티 생성 및 저장
        T ledger = createLedgerEntity(eventData);
        ledgerRepository.save(ledger);
        log.info("[Kafka Consumer] Ledger synced successfully: ledgerId={}, name={}",
                ledger.getLedgerId(), ledger.getName());
    }

    /**
     * LEDGER_UPDATED 이벤트 처리
     */
    private void handleLedgerUpdated(Map<String, Object> eventData) {
        Long ledgerId = extractLedgerId(eventData);
        log.info("[Kafka Consumer] Processing LEDGER_UPDATED: ledgerId={}", ledgerId);

        ledgerRepository.findByLedgerIdAndIsDeletedFalse(ledgerId).ifPresentOrElse(
                ledger -> {
                    ledger.updateFromLedgerService(
                            extractString(eventData, "name"),
                            extractString(eventData, "description"),
                            extractString(eventData, "currency"),
                            extractBoolean(eventData, "isDefault")
                    );
                    ledgerRepository.save(ledger);
                    log.info("[Kafka Consumer] Ledger updated successfully: ledgerId={}", ledgerId);
                },
                () -> {
                    // 없으면 새로 생성
                    T ledger = createLedgerEntity(eventData);
                    ledgerRepository.save(ledger);
                    log.info("[Kafka Consumer] Ledger created from update event: ledgerId={}", ledgerId);
                }
        );
    }

    /**
     * LEDGER_DELETED 이벤트 처리
     */
    private void handleLedgerDeleted(Map<String, Object> eventData) {
        Long ledgerId = extractLedgerId(eventData);
        log.info("[Kafka Consumer] Processing LEDGER_DELETED: ledgerId={}", ledgerId);

        ledgerRepository.findByLedgerIdAndIsDeletedFalse(ledgerId).ifPresentOrElse(
                ledger -> {
                    ledger.delete();
                    ledgerRepository.save(ledger);
                    log.info("[Kafka Consumer] Ledger deleted successfully: ledgerId={}", ledgerId);
                },
                () -> log.warn("[Kafka Consumer] Ledger not found for deletion: ledgerId={}", ledgerId)
        );
    }

    /**
     * 이벤트 데이터에서 ledgerId 추출
     */
    protected Long extractLedgerId(Map<String, Object> eventData) {
        Object ledgerId = eventData.get("ledgerId");
        if (ledgerId instanceof Number) {
            return ((Number) ledgerId).longValue();
        }
        if (ledgerId instanceof String) {
            return Long.parseLong((String) ledgerId);
        }
        throw new IllegalArgumentException("Invalid ledgerId format: " + ledgerId);
    }

    /**
     * 이벤트 데이터에서 userId 추출
     */
    protected Long extractUserId(Map<String, Object> eventData) {
        Object userId = eventData.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        if (userId instanceof String) {
            return Long.parseLong((String) userId);
        }
        throw new IllegalArgumentException("Invalid userId format: " + userId);
    }

    /**
     * 이벤트 데이터에서 문자열 추출
     */
    protected String extractString(Map<String, Object> eventData, String key) {
        Object value = eventData.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 이벤트 데이터에서 Boolean 추출
     */
    protected Boolean extractBoolean(Map<String, Object> eventData, String key) {
        Object value = eventData.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }

    /**
     * 가계부 엔티티 생성 (서비스별 구현 필요)
     *
     * @param eventData 이벤트 데이터
     * @return 생성된 Ledger 엔티티
     */
    protected abstract T createLedgerEntity(Map<String, Object> eventData);
}
