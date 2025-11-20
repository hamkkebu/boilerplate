package com.hamkkebu.boilerplate.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting 서비스 (Redis 기반 Sliding Window)
 *
 * <p>API 호출 횟수 제한을 통해 DDoS 공격 및 무차별 대입 공격을 방어합니다.</p>
 *
 * <p>Rate Limit 정책:</p>
 * <ul>
 *   <li>인증 API: 5 requests / 1분 (로그인, 회원가입 등)</li>
 *   <li>일반 API: 100 requests / 1분</li>
 *   <li>Redis 장애 시: Rate Limiting 비활성화 (가용성 우선)</li>
 * </ul>
 *
 * <p>구현 방식: Sliding Window Counter (Redis INCR + EXPIRE)</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitingService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Value("${security.rate-limiting.auth-requests-per-minute:5}")
    private int authRequestsPerMinute;

    @Value("${security.rate-limiting.general-requests-per-minute:100}")
    private int generalRequestsPerMinute;

    @Value("${security.rate-limiting.window-size-seconds:60}")
    private long windowSizeSeconds;

    /**
     * Rate Limit 체크 (인증 API용)
     *
     * @param key 사용자 식별자 (IP 또는 userId)
     * @return 허용되면 true, 제한 초과 시 false
     */
    public boolean tryConsumeAuth(String key) {
        return tryConsume(key, authRequestsPerMinute);
    }

    /**
     * Rate Limit 체크 (일반 API용)
     *
     * @param key 사용자 식별자 (IP 또는 userId)
     * @return 허용되면 true, 제한 초과 시 false
     */
    public boolean tryConsumeGeneral(String key) {
        return tryConsume(key, generalRequestsPerMinute);
    }

    /**
     * Rate Limit 체크 (내부 메서드)
     *
     * <p>Sliding Window Counter 알고리즘:</p>
     * <ol>
     *   <li>Redis INCR로 카운터 증가</li>
     *   <li>첫 요청이면 EXPIRE 설정 (TTL 60초)</li>
     *   <li>카운터 값이 limit 이하이면 허용</li>
     * </ol>
     *
     * @param key 사용자 식별자
     * @param limit 분당 요청 제한
     * @return 허용되면 true, 제한 초과 시 false
     */
    private boolean tryConsume(String key, int limit) {
        try {
            String redisKey = RATE_LIMIT_PREFIX + key;

            // 1. 카운터 증가
            Long count = redisTemplate.opsForValue().increment(redisKey);

            if (count == null) {
                // Redis 오류 시 허용 (가용성 우선)
                log.warn("Redis increment returned null for key: {}", redisKey);
                return true;
            }

            // 2. 첫 요청이면 TTL 설정
            if (count == 1) {
                redisTemplate.expire(redisKey, windowSizeSeconds, TimeUnit.SECONDS);
            }

            // 3. Limit 체크
            if (count > limit) {
                log.warn("Rate limit exceeded: key={}, count={}, limit={}/min",
                        key, count, limit);
                return false;
            }

            return true;

        } catch (RedisConnectionFailureException e) {
            // Redis 장애 시 Rate Limiting 비활성화 (가용성 우선)
            log.warn("Redis connection failed during rate limiting: key={}, error={}. Allowing request.",
                    key, e.getMessage());
            return true;
        } catch (Exception e) {
            // 기타 예외 시에도 허용 (가용성 우선)
            log.error("Unexpected error during rate limiting: key={}, error={}. Allowing request.",
                    key, e.getMessage(), e);
            return true;
        }
    }

    /**
     * Rate Limiting 활성화 여부 (항상 true, Redis 장애 시만 동작 안함)
     *
     * @return true
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * 특정 키의 Rate Limit 초기화 (테스트용)
     *
     * @param key 사용자 식별자
     */
    public void reset(String key) {
        try {
            String redisKey = RATE_LIMIT_PREFIX + key;
            redisTemplate.delete(redisKey);
            log.info("Rate limit reset: key={}", key);
        } catch (Exception e) {
            log.warn("Failed to reset rate limit: key={}, error={}", key, e.getMessage());
        }
    }
}
