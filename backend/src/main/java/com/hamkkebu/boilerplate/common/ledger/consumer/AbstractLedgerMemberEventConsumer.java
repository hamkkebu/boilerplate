package com.hamkkebu.boilerplate.common.ledger.consumer;

import com.hamkkebu.boilerplate.common.enums.MemberRole;
import com.hamkkebu.boilerplate.common.ledger.entity.SyncedLedgerMember;
import com.hamkkebu.boilerplate.common.ledger.event.LedgerMemberAddedEvent;
import com.hamkkebu.boilerplate.common.ledger.event.LedgerMemberRemovedEvent;
import com.hamkkebu.boilerplate.common.ledger.event.LedgerMemberRoleChangedEvent;
import com.hamkkebu.boilerplate.common.ledger.repository.SyncedLedgerMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 가계부 멤버 이벤트 Kafka Consumer 추상 클래스
 *
 * <p>ledger-service에서 발행한 가계부 멤버 관련 이벤트를 수신합니다.</p>
 * <ul>
 *   <li>LEDGER_MEMBER_ADDED: 신규 멤버 추가 동기화</li>
 *   <li>LEDGER_MEMBER_REMOVED: 멤버 제거 (soft delete)</li>
 *   <li>LEDGER_MEMBER_ROLE_CHANGED: 멤버 역할 변경</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Slf4j
 * @Component
 * public class LedgerMemberEventConsumer extends AbstractLedgerMemberEventConsumer<LedgerMember> {
 *
 *     public LedgerMemberEventConsumer(LedgerMemberRepository repo) {
 *         super(repo);
 *     }
 *
 *     @Override
 *     protected LedgerMember createLedgerMemberEntity(Map<String, Object> eventData) {
 *         return LedgerMember.builder()
 *             .ledgerMemberId(extractLedgerMemberId(eventData))
 *             .ledgerId(extractLedgerId(eventData))
 *             .accountId(extractAccountId(eventData))
 *             .role(extractRole(eventData))
 *             .build();
 *     }
 *
 *     @KafkaListener(
 *             topics = "${kafka.topics.ledger-member-events:ledger-member.events}",
 *             groupId = "transaction-service-group",
 *             containerFactory = "transactionKafkaListenerContainerFactory"
 *     )
 *     @Transactional
 *     public void handleLedgerMemberEvent(Map<String, Object> eventData) {
 *         processLedgerMemberEvent(eventData);
 *     }
 * }
 * }
 * </pre>
 *
 * @param <T> SyncedLedgerMember를 상속받은 엔티티 타입
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLedgerMemberEventConsumer<T extends SyncedLedgerMember> {

    private final SyncedLedgerMemberRepository<T> ledgerMemberRepository;

    /**
     * 가계부 멤버 이벤트 처리 (서브클래스에서 KafkaListener와 함께 호출)
     *
     * @param eventData 이벤트 데이터 맵
     */
    @Transactional
    protected void processLedgerMemberEvent(Map<String, Object> eventData) {
        String eventType = (String) eventData.get("eventType");
        String eventId = (String) eventData.get("eventId");

        log.info("[Kafka Consumer] Received ledger member event: eventType={}, eventId={}", eventType, eventId);

        if (eventType == null) {
            log.warn("[Kafka Consumer] Event type is null, skipping event: eventId={}", eventId);
            return;
        }

        try {
            if (LedgerMemberAddedEvent.EVENT_TYPE.equals(eventType)) {
                handleLedgerMemberAdded(eventData);
            } else if (LedgerMemberRemovedEvent.EVENT_TYPE.equals(eventType)) {
                handleLedgerMemberRemoved(eventData);
            } else if (LedgerMemberRoleChangedEvent.EVENT_TYPE.equals(eventType)) {
                handleLedgerMemberRoleChanged(eventData);
            } else {
                log.warn("[Kafka Consumer] Unknown ledger member event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("[Kafka Consumer] Failed to process ledger member event: eventType={}, eventId={}, error={}",
                    eventType, eventId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * LEDGER_MEMBER_ADDED 이벤트 처리
     *
     * <p>멱등성 보장: 중복 이벤트 수신 시 DataIntegrityViolationException을 catch하여 무시합니다.</p>
     */
    private void handleLedgerMemberAdded(Map<String, Object> eventData) {
        Long ledgerMemberId = extractLedgerMemberId(eventData);
        Long ledgerId = extractLedgerId(eventData);
        Long accountId = extractAccountId(eventData);

        log.info("[Kafka Consumer] Processing LEDGER_MEMBER_ADDED: memberId={}, ledgerId={}, accountId={}",
                ledgerMemberId, ledgerId, accountId);

        // 이미 존재하는 멤버인지 확인
        if (ledgerMemberRepository.findById(ledgerMemberId).isPresent()) {
            log.info("[Kafka Consumer] Ledger member already exists (idempotent skip): memberId={}", ledgerMemberId);
            return;
        }

        // 멤버 엔티티 생성 및 저장 (race condition 대비 try-catch)
        try {
            T member = createLedgerMemberEntity(eventData);
            ledgerMemberRepository.save(member);
            log.info("[Kafka Consumer] Ledger member synced successfully: memberId={}, ledgerId={}, accountId={}",
                    ledgerMemberId, ledgerId, accountId);
        } catch (DataIntegrityViolationException e) {
            // 동시에 처리된 중복 이벤트 (race condition) — 안전하게 무시
            log.info("[Kafka Consumer] Ledger member already exists (concurrent duplicate): memberId={}", ledgerMemberId);
        }
    }

    /**
     * LEDGER_MEMBER_REMOVED 이벤트 처리
     *
     * <p>멱등성 보장: 이미 삭제된 상태이면 무시합니다.</p>
     */
    private void handleLedgerMemberRemoved(Map<String, Object> eventData) {
        Long ledgerMemberId = extractLedgerMemberId(eventData);
        log.info("[Kafka Consumer] Processing LEDGER_MEMBER_REMOVED: memberId={}", ledgerMemberId);

        ledgerMemberRepository.findById(ledgerMemberId).ifPresentOrElse(
                member -> {
                    // 멱등성: 이미 삭제된 상태이면 스킵
                    if (member.isDeleted()) {
                        log.info("[Kafka Consumer] Ledger member already deleted (idempotent skip): memberId={}", ledgerMemberId);
                        return;
                    }
                    member.delete();
                    ledgerMemberRepository.save(member);
                    log.info("[Kafka Consumer] Ledger member deleted successfully: memberId={}", ledgerMemberId);
                },
                () -> log.warn("[Kafka Consumer] Ledger member not found for deletion: memberId={}", ledgerMemberId)
        );
    }

    /**
     * LEDGER_MEMBER_ROLE_CHANGED 이벤트 처리
     *
     * <p>멱등성 보장: 이미 같은 역할을 가지고 있으면 무시합니다.</p>
     */
    private void handleLedgerMemberRoleChanged(Map<String, Object> eventData) {
        Long ledgerMemberId = extractLedgerMemberId(eventData);
        log.info("[Kafka Consumer] Processing LEDGER_MEMBER_ROLE_CHANGED: memberId={}", ledgerMemberId);

        ledgerMemberRepository.findById(ledgerMemberId).ifPresentOrElse(
                member -> {
                    MemberRole newRole = extractRole(eventData);
                    // 멱등성: 이미 같은 역할이면 스킵
                    if (member.hasRole(newRole)) {
                        log.info("[Kafka Consumer] Ledger member already has role {} (idempotent skip): memberId={}",
                                newRole, ledgerMemberId);
                        return;
                    }
                    member.updateRole(newRole);
                    ledgerMemberRepository.save(member);
                    log.info("[Kafka Consumer] Ledger member role changed successfully: memberId={}, newRole={}",
                            ledgerMemberId, newRole);
                },
                () -> log.warn("[Kafka Consumer] Ledger member not found for role change: memberId={}", ledgerMemberId)
        );
    }

    // ==================== 데이터 추출 유틸리티 ====================

    /**
     * 이벤트 데이터에서 ledgerMemberId 추출
     *
     * @throws IllegalArgumentException 값이 null이거나 유효하지 않은 형식인 경우
     */
    protected Long extractLedgerMemberId(Map<String, Object> eventData) {
        return extractLong(eventData, "ledgerMemberId");
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
     * 이벤트 데이터에서 accountId 추출
     *
     * @throws IllegalArgumentException 값이 null이거나 유효하지 않은 형식인 경우
     */
    protected Long extractAccountId(Map<String, Object> eventData) {
        return extractLong(eventData, "accountId");
    }

    /**
     * 이벤트 데이터에서 MemberRole 추출 (기본값: MEMBER)
     */
    protected MemberRole extractRole(Map<String, Object> eventData) {
        String roleStr = extractString(eventData, "role");
        if (roleStr == null) {
            return MemberRole.MEMBER;
        }
        try {
            return MemberRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            log.warn("[Kafka Consumer] Invalid role value: {}, using default MEMBER", roleStr);
            return MemberRole.MEMBER;
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
     * 가계부 멤버 엔티티 생성 (서비스별 구현 필요)
     *
     * @param eventData 이벤트 데이터
     * @return 생성된 LedgerMember 엔티티
     */
    protected abstract T createLedgerMemberEntity(Map<String, Object> eventData);
}
