package com.hamkkebu.boilerplate.common.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Kafka Consumer Idempotency Service
 *
 * <p>중복 이벤트 처리 방지를 위한 서비스</p>
 *
 * <p>동작 방식:</p>
 * <ul>
 *   <li>이벤트 처리 전 eventId가 Redis에 존재하는지 확인</li>
 *   <li>존재하면 중복 이벤트로 판단하고 처리 스킵</li>
 *   <li>존재하지 않으면 처리 후 Redis에 eventId 저장 (TTL 적용)</li>
 * </ul>
 *
 * <p>TTL 설정:</p>
 * <ul>
 *   <li>기본값: 7일 (604800초)</li>
 *   <li>Kafka retention 기간과 동일하게 설정 권장</li>
 *   <li>환경 변수: kafka.idempotency.ttl-hours (기본값: 168시간 = 7일)</li>
 * </ul>
 *
 * <p>Redis Key 형식:</p>
 * <ul>
 *   <li>Pattern: kafka:idempotency:{topic}:{eventId}</li>
 *   <li>예시: kafka:idempotency:user.events:a1b2c3d4-e5f6-7890-abcd-ef1234567890</li>
 * </ul>
 *
 * <p>주의사항:</p>
 * <ul>
 *   <li>모든 이벤트는 고유한 eventId를 가져야 함 (UUID 권장)</li>
 *   <li>동일한 이벤트를 다시 발행하려면 새로운 eventId를 생성해야 함</li>
 *   <li>Redis 장애 시 중복 처리 가능성 존재 (비즈니스 로직에서 추가 검증 필요)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    private static final String IDEMPOTENCY_KEY_PREFIX = "kafka:idempotency:";
    private static final String PROCESSED_VALUE = "PROCESSED";

    /**
     * TTL 설정 (7일)
     * Kafka retention 기간과 동일하게 설정
     * 환경 변수로 커스터마이징 가능: kafka.idempotency.ttl-hours
     */
    @org.springframework.beans.factory.annotation.Value("${kafka.idempotency.ttl-hours:168}")
    private int ttlHours;

    /**
     * 이벤트가 이미 처리되었는지 확인 (NON-ATOMIC)
     *
     * ⚠️ DEPRECATED: Race condition 발생 가능
     * → tryMarkAsProcessed() 사용 권장
     *
     * @param topic Kafka 토픽명
     * @param eventId 이벤트 ID
     * @return 이미 처리된 이벤트면 true, 아니면 false
     * @deprecated Use {@link #tryMarkAsProcessed(String, String)} instead for atomic check-and-set
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public boolean isAlreadyProcessed(String topic, String eventId) {
        String key = buildKey(topic, eventId);
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            log.warn("Duplicate event detected and skipped: topic={}, eventId={}", topic, eventId);
            return true;
        }

        return false;
    }

    /**
     * 이벤트를 처리 완료로 표시 (NON-ATOMIC)
     *
     * ⚠️ DEPRECATED: isAlreadyProcessed()와 함께 사용 시 race condition 발생
     * → tryMarkAsProcessed() 사용 권장
     *
     * @param topic Kafka 토픽명
     * @param eventId 이벤트 ID
     * @deprecated Use {@link #tryMarkAsProcessed(String, String)} instead for atomic check-and-set
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void markAsProcessed(String topic, String eventId) {
        String key = buildKey(topic, eventId);
        Duration ttl = Duration.ofHours(ttlHours);

        redisTemplate.opsForValue().set(key, PROCESSED_VALUE, ttl);

        log.debug("Event marked as processed: topic={}, eventId={}, ttl={}h", topic, eventId, ttlHours);
    }

    /**
     * Atomic check-and-set: 이벤트 처리 시도 (Redis SETNX 사용)
     *
     * <p>RACE CONDITION SAFE: Redis의 SET NX (Set if Not eXists) 명령으로
     * 원자적으로 중복 체크와 마킹을 동시에 수행합니다.</p>
     *
     * <p>동작 방식:</p>
     * <ul>
     *   <li>Key가 존재하지 않으면: SET 성공, true 반환 → 이벤트 처리 진행</li>
     *   <li>Key가 이미 존재하면: SET 실패, false 반환 → 이벤트 스킵</li>
     * </ul>
     *
     * <p>사용 예시:</p>
     * <pre>
     * if (!idempotencyService.tryMarkAsProcessed(TOPIC, event.getEventId())) {
     *     log.info("Duplicate event, skipping");
     *     return;  // 중복 이벤트, 처리하지 않음
     * }
     * // 이벤트 처리 - 중복 불가능 보장
     * processEvent(event);
     * </pre>
     *
     * @param topic Kafka 토픽명
     * @param eventId 이벤트 ID
     * @return 처리 가능하면 true (첫 시도), 중복이면 false
     */
    public boolean tryMarkAsProcessed(String topic, String eventId) {
        String key = buildKey(topic, eventId);
        Duration ttl = Duration.ofHours(ttlHours);

        // Redis SET NX (Set if Not eXists) - Atomic operation
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(key, PROCESSED_VALUE, ttl);

        if (Boolean.TRUE.equals(wasSet)) {
            log.debug("Event marked as processed (atomic): topic={}, eventId={}, ttl={}h",
                     topic, eventId, ttlHours);
            return true;  // 처리 가능 (첫 시도)
        } else {
            log.warn("Duplicate event detected and skipped (atomic): topic={}, eventId={}",
                    topic, eventId);
            return false;  // 중복 이벤트
        }
    }

    /**
     * Atomic check-and-set with custom TTL
     *
     * @param topic Kafka 토픽명
     * @param eventId 이벤트 ID
     * @param ttl TTL 기간
     * @return 처리 가능하면 true, 중복이면 false
     */
    public boolean tryMarkAsProcessed(String topic, String eventId, Duration ttl) {
        String key = buildKey(topic, eventId);

        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(key, PROCESSED_VALUE, ttl);

        if (Boolean.TRUE.equals(wasSet)) {
            log.debug("Event marked as processed (atomic): topic={}, eventId={}, ttl={}",
                     topic, eventId, ttl);
            return true;
        } else {
            log.warn("Duplicate event detected and skipped (atomic): topic={}, eventId={}",
                    topic, eventId);
            return false;
        }
    }

    /**
     * 이벤트를 처리 완료로 표시 (커스텀 TTL)
     *
     * @param topic Kafka 토픽명
     * @param eventId 이벤트 ID
     * @param ttl TTL 기간
     */
    public void markAsProcessed(String topic, String eventId, Duration ttl) {
        String key = buildKey(topic, eventId);
        redisTemplate.opsForValue().set(key, PROCESSED_VALUE, ttl);

        log.debug("Event marked as processed: topic={}, eventId={}, ttl={}", topic, eventId, ttl);
    }

    /**
     * Redis Key 생성
     *
     * @param topic Kafka 토픽명
     * @param eventId 이벤트 ID
     * @return Redis Key
     */
    private String buildKey(String topic, String eventId) {
        return IDEMPOTENCY_KEY_PREFIX + topic + ":" + eventId;
    }

    /**
     * 특정 이벤트의 처리 상태 삭제 (테스트용)
     * 프로덕션에서는 사용하지 않음
     *
     * @param topic Kafka 토픽명
     * @param eventId 이벤트 ID
     */
    public void removeProcessedStatus(String topic, String eventId) {
        String key = buildKey(topic, eventId);
        redisTemplate.delete(key);
        log.warn("Processed status removed: topic={}, eventId={} (TEST ONLY)", topic, eventId);
    }
}
