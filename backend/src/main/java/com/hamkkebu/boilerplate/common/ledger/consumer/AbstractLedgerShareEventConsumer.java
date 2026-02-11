package com.hamkkebu.boilerplate.common.ledger.consumer;

import com.hamkkebu.boilerplate.common.enums.SharePermission;
import com.hamkkebu.boilerplate.common.enums.ShareStatus;
import com.hamkkebu.boilerplate.common.ledger.entity.SyncedLedgerShare;
import com.hamkkebu.boilerplate.common.ledger.event.LedgerShareAcceptedEvent;
import com.hamkkebu.boilerplate.common.ledger.event.LedgerShareCreatedEvent;
import com.hamkkebu.boilerplate.common.ledger.event.LedgerShareDeletedEvent;
import com.hamkkebu.boilerplate.common.ledger.event.LedgerShareRejectedEvent;
import com.hamkkebu.boilerplate.common.ledger.repository.SyncedLedgerShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 가계부 공유 이벤트 Kafka Consumer 추상 클래스
 *
 * <p>ledger-service에서 발행한 가계부 공유 관련 이벤트를 수신합니다.</p>
 * <ul>
 *   <li>LEDGER_SHARE_CREATED: 신규 공유 요청 동기화</li>
 *   <li>LEDGER_SHARE_ACCEPTED: 공유 수락 상태 업데이트</li>
 *   <li>LEDGER_SHARE_REJECTED: 공유 거절 상태 업데이트</li>
 *   <li>LEDGER_SHARE_DELETED: 공유 삭제 (soft delete)</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Slf4j
 * @Component
 * public class LedgerShareEventConsumer extends AbstractLedgerShareEventConsumer<LedgerShare> {
 *
 *     public LedgerShareEventConsumer(LedgerShareRepository repo) {
 *         super(repo);
 *     }
 *
 *     @Override
 *     protected LedgerShare createLedgerShareEntity(Map<String, Object> eventData) {
 *         return LedgerShare.builder()
 *             .ledgerShareId(extractLedgerShareId(eventData))
 *             .ledgerId(extractLedgerId(eventData))
 *             .ownerId(extractOwnerId(eventData))
 *             .sharedUserId(extractSharedUserId(eventData))
 *             .status(extractStatus(eventData))
 *             .permission(extractPermission(eventData))
 *             .build();
 *     }
 *
 *     @KafkaListener(
 *             topics = "${kafka.topics.ledger-share-events:ledger-share.events}",
 *             groupId = "transaction-service-group",
 *             containerFactory = "transactionKafkaListenerContainerFactory"
 *     )
 *     @Transactional
 *     public void handleLedgerShareEvent(Map<String, Object> eventData) {
 *         processLedgerShareEvent(eventData);
 *     }
 * }
 * }
 * </pre>
 *
 * @param <T> SyncedLedgerShare를 상속받은 엔티티 타입
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLedgerShareEventConsumer<T extends SyncedLedgerShare> {

    private final SyncedLedgerShareRepository<T> ledgerShareRepository;

    /**
     * 가계부 공유 이벤트 처리 (서브클래스에서 KafkaListener와 함께 호출)
     *
     * @param eventData 이벤트 데이터 맵
     */
    @Transactional
    protected void processLedgerShareEvent(Map<String, Object> eventData) {
        String eventType = (String) eventData.get("eventType");
        String eventId = (String) eventData.get("eventId");

        log.info("[Kafka Consumer] Received ledger share event: eventType={}, eventId={}", eventType, eventId);

        if (eventType == null) {
            log.warn("[Kafka Consumer] Event type is null, skipping event: eventId={}", eventId);
            return;
        }

        try {
            if (LedgerShareCreatedEvent.EVENT_TYPE.equals(eventType)) {
                handleLedgerShareCreated(eventData);
            } else if (LedgerShareAcceptedEvent.EVENT_TYPE.equals(eventType)) {
                handleLedgerShareAccepted(eventData);
            } else if (LedgerShareRejectedEvent.EVENT_TYPE.equals(eventType)) {
                handleLedgerShareRejected(eventData);
            } else if (LedgerShareDeletedEvent.EVENT_TYPE.equals(eventType)) {
                handleLedgerShareDeleted(eventData);
            } else {
                log.warn("[Kafka Consumer] Unknown ledger share event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("[Kafka Consumer] Failed to process ledger share event: eventType={}, eventId={}, error={}",
                    eventType, eventId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * LEDGER_SHARE_CREATED 이벤트 처리
     *
     * <p>멱등성 보장: 중복 이벤트 수신 시 DataIntegrityViolationException을 catch하여 무시합니다.</p>
     */
    private void handleLedgerShareCreated(Map<String, Object> eventData) {
        Long ledgerShareId = extractLedgerShareId(eventData);
        Long ledgerId = extractLedgerId(eventData);
        Long sharedUserId = extractSharedUserId(eventData);

        log.info("[Kafka Consumer] Processing LEDGER_SHARE_CREATED: shareId={}, ledgerId={}, sharedUserId={}",
                ledgerShareId, ledgerId, sharedUserId);

        // 이미 존재하는 공유인지 확인
        if (ledgerShareRepository.findById(ledgerShareId).isPresent()) {
            log.info("[Kafka Consumer] Ledger share already exists (idempotent skip): shareId={}", ledgerShareId);
            return;
        }

        // 공유 엔티티 생성 및 저장 (race condition 대비 try-catch)
        try {
            T share = createLedgerShareEntity(eventData);
            ledgerShareRepository.save(share);
            log.info("[Kafka Consumer] Ledger share synced successfully: shareId={}, ledgerId={}, sharedUserId={}",
                    ledgerShareId, ledgerId, sharedUserId);
        } catch (DataIntegrityViolationException e) {
            // 동시에 처리된 중복 이벤트 (race condition) — 안전하게 무시
            log.info("[Kafka Consumer] Ledger share already exists (concurrent duplicate): shareId={}", ledgerShareId);
        }
    }

    /**
     * LEDGER_SHARE_ACCEPTED 이벤트 처리
     *
     * <p>멱등성 보장: 이미 ACCEPTED 상태이면 무시합니다.</p>
     */
    private void handleLedgerShareAccepted(Map<String, Object> eventData) {
        Long ledgerShareId = extractLedgerShareId(eventData);
        log.info("[Kafka Consumer] Processing LEDGER_SHARE_ACCEPTED: shareId={}", ledgerShareId);

        ledgerShareRepository.findById(ledgerShareId).ifPresentOrElse(
                share -> {
                    // 멱등성: 이미 수락된 상태이면 스킵
                    if (share.isAccepted()) {
                        log.info("[Kafka Consumer] Ledger share already accepted (idempotent skip): shareId={}", ledgerShareId);
                        return;
                    }
                    share.updateFromEvent(ShareStatus.ACCEPTED, share.getPermission(), LocalDateTime.now());
                    ledgerShareRepository.save(share);
                    log.info("[Kafka Consumer] Ledger share accepted successfully: shareId={}", ledgerShareId);
                },
                () -> log.warn("[Kafka Consumer] Ledger share not found for acceptance: shareId={}", ledgerShareId)
        );
    }

    /**
     * LEDGER_SHARE_REJECTED 이벤트 처리
     *
     * <p>멱등성 보장: 이미 REJECTED 상태이면 무시합니다.</p>
     */
    private void handleLedgerShareRejected(Map<String, Object> eventData) {
        Long ledgerShareId = extractLedgerShareId(eventData);
        log.info("[Kafka Consumer] Processing LEDGER_SHARE_REJECTED: shareId={}", ledgerShareId);

        ledgerShareRepository.findById(ledgerShareId).ifPresentOrElse(
                share -> {
                    // 멱등성: 이미 거절된 상태이면 스킵
                    if (share.isRejected()) {
                        log.info("[Kafka Consumer] Ledger share already rejected (idempotent skip): shareId={}", ledgerShareId);
                        return;
                    }
                    share.updateFromEvent(ShareStatus.REJECTED, share.getPermission(), null);
                    ledgerShareRepository.save(share);
                    log.info("[Kafka Consumer] Ledger share rejected successfully: shareId={}", ledgerShareId);
                },
                () -> log.warn("[Kafka Consumer] Ledger share not found for rejection: shareId={}", ledgerShareId)
        );
    }

    /**
     * LEDGER_SHARE_DELETED 이벤트 처리
     *
     * <p>멱등성 보장: 이미 삭제된 상태이면 무시합니다.</p>
     */
    private void handleLedgerShareDeleted(Map<String, Object> eventData) {
        Long ledgerShareId = extractLedgerShareId(eventData);
        log.info("[Kafka Consumer] Processing LEDGER_SHARE_DELETED: shareId={}", ledgerShareId);

        ledgerShareRepository.findById(ledgerShareId).ifPresentOrElse(
                share -> {
                    // 멱등성: 이미 삭제된 상태이면 스킵
                    if (share.isDeleted()) {
                        log.info("[Kafka Consumer] Ledger share already deleted (idempotent skip): shareId={}", ledgerShareId);
                        return;
                    }
                    share.delete();
                    ledgerShareRepository.save(share);
                    log.info("[Kafka Consumer] Ledger share deleted successfully: shareId={}", ledgerShareId);
                },
                () -> log.warn("[Kafka Consumer] Ledger share not found for deletion: shareId={}", ledgerShareId)
        );
    }

    // ==================== 데이터 추출 유틸리티 ====================

    /**
     * 이벤트 데이터에서 ledgerShareId 추출
     *
     * @throws IllegalArgumentException 값이 null이거나 유효하지 않은 형식인 경우
     */
    protected Long extractLedgerShareId(Map<String, Object> eventData) {
        return extractLong(eventData, "ledgerShareId");
    }

    /**
     * 이벤트 데이터에서 ledgerId 추출
     *
     * @throws IllegalArgumentException 값이 null이거나 유효하지 않은 형식인 경우
     */
    protected Long extractLedgerId(Map<String, Object> eventData) {
        return extractLong(eventData, "ledgerId");
    }

    /**
     * 이벤트 데이터에서 ownerId 추출
     *
     * @throws IllegalArgumentException 값이 null이거나 유효하지 않은 형식인 경우
     */
    protected Long extractOwnerId(Map<String, Object> eventData) {
        return extractLong(eventData, "ownerId");
    }

    /**
     * 이벤트 데이터에서 sharedUserId 추출
     *
     * @throws IllegalArgumentException 값이 null이거나 유효하지 않은 형식인 경우
     */
    protected Long extractSharedUserId(Map<String, Object> eventData) {
        return extractLong(eventData, "sharedUserId");
    }

    /**
     * 이벤트 데이터에서 ShareStatus 추출 (기본값: PENDING)
     */
    protected ShareStatus extractStatus(Map<String, Object> eventData) {
        String statusStr = extractString(eventData, "status");
        if (statusStr == null) {
            return ShareStatus.PENDING;
        }
        try {
            return ShareStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            log.warn("[Kafka Consumer] Invalid status value: {}, using default PENDING", statusStr);
            return ShareStatus.PENDING;
        }
    }

    /**
     * 이벤트 데이터에서 SharePermission 추출 (기본값: READ_ONLY)
     */
    protected SharePermission extractPermission(Map<String, Object> eventData) {
        String permissionStr = extractString(eventData, "permission");
        if (permissionStr == null) {
            return SharePermission.READ_ONLY;
        }
        try {
            return SharePermission.valueOf(permissionStr);
        } catch (IllegalArgumentException e) {
            log.warn("[Kafka Consumer] Invalid permission value: {}, using default READ_ONLY", permissionStr);
            return SharePermission.READ_ONLY;
        }
    }

    /**
     * 이벤트 데이터에서 문자열 추출
     */
    protected String extractString(Map<String, Object> eventData, String key) {
        Object value = eventData.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 이벤트 데이터에서 Long 값 추출 (공통 유틸리티)
     *
     * @param eventData 이벤트 데이터
     * @param key       추출할 키
     * @return Long 값
     * @throws IllegalArgumentException 값이 null이거나 유효하지 않은 형식인 경우
     */
    private Long extractLong(Map<String, Object> eventData, String key) {
        Object value = eventData.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Required field '" + key + "' is missing from event data");
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid " + key + " format: " + value, e);
            }
        }
        throw new IllegalArgumentException("Invalid " + key + " type: " + value.getClass().getSimpleName());
    }

    /**
     * 가계부 공유 엔티티 생성 (서비스별 구현 필요)
     *
     * @param eventData 이벤트 데이터
     * @return 생성된 LedgerShare 엔티티
     */
    protected abstract T createLedgerShareEntity(Map<String, Object> eventData);
}
