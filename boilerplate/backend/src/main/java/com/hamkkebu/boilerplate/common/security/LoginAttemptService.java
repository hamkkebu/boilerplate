package com.hamkkebu.boilerplate.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 로그인 시도 횟수 제한 서비스
 *
 * SECURITY: 브루트포스 공격 방어
 * - Redis 기반 로그인 시도 추적
 * - 설정 가능한 최대 시도 횟수 및 잠금 시간
 * - Redis 장애 시 Fallback (통과)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";
    private static final String BLOCKED_PREFIX = "login_blocked:";

    @Value("${security.login-attempt.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login-attempt.block-duration-minutes:15}")
    private long blockDurationMinutes;

    @Value("${security.login-attempt.attempt-reset-minutes:15}")
    private long attemptResetMinutes;

    /**
     * 로그인 시도 기록
     *
     * @param userId 사용자 ID
     * @return 현재 시도 횟수
     */
    public int recordLoginAttempt(String userId) {
        try {
            String key = LOGIN_ATTEMPT_PREFIX + userId;
            Long attempts = redisTemplate.opsForValue().increment(key);

            if (attempts == null) {
                attempts = 1L;
            }

            if (attempts == 1) {
                // 첫 시도: TTL 설정
                redisTemplate.expire(key, attemptResetMinutes, TimeUnit.MINUTES);
            }

            log.debug("Login attempt recorded: userId={}, attempts={}", userId, attempts);

            // maxAttempts 도달 시 계정 잠금
            if (attempts >= maxAttempts) {
                blockUser(userId);
            }

            return attempts.intValue();
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed while recording login attempt: {}", e.getMessage());
            // Redis 장애 시 통과 (가용성 우선)
            return 0;
        }
    }

    /**
     * 사용자 계정 잠금
     *
     * @param userId 사용자 ID
     */
    private void blockUser(String userId) {
        try {
            String key = BLOCKED_PREFIX + userId;
            redisTemplate.opsForValue().set(key, "blocked", blockDurationMinutes, TimeUnit.MINUTES);
            log.warn("User blocked due to too many login attempts: userId={}, duration={}min",
                    userId, blockDurationMinutes);
        } catch (RedisConnectionFailureException e) {
            log.error("Failed to block user (Redis error): {}", e.getMessage());
        }
    }

    /**
     * 사용자 차단 여부 확인
     *
     * @param userId 사용자 ID
     * @return 차단되었으면 true
     */
    public boolean isBlocked(String userId) {
        try {
            String key = BLOCKED_PREFIX + userId;
            Boolean hasKey = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(hasKey);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed while checking block status: {}", e.getMessage());
            // Redis 장애 시 통과 (가용성 우선)
            return false;
        }
    }

    /**
     * 로그인 성공 시 시도 횟수 초기화
     *
     * @param userId 사용자 ID
     */
    public void resetLoginAttempts(String userId) {
        try {
            String attemptKey = LOGIN_ATTEMPT_PREFIX + userId;
            String blockedKey = BLOCKED_PREFIX + userId;

            redisTemplate.delete(attemptKey);
            redisTemplate.delete(blockedKey);

            log.debug("Login attempts reset: userId={}", userId);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed while resetting login attempts: {}", e.getMessage());
        }
    }

    /**
     * 남은 시도 횟수 조회
     *
     * @param userId 사용자 ID
     * @return 남은 시도 횟수
     */
    public int getRemainingAttempts(String userId) {
        try {
            String key = LOGIN_ATTEMPT_PREFIX + userId;
            String attempts = redisTemplate.opsForValue().get(key);

            if (attempts == null) {
                return maxAttempts;
            }

            int currentAttempts = Integer.parseInt(attempts);
            return Math.max(0, maxAttempts - currentAttempts);
        } catch (Exception e) {
            log.warn("Failed to get remaining attempts: {}", e.getMessage());
            return maxAttempts; // 에러 시 최대 횟수 반환
        }
    }
}
