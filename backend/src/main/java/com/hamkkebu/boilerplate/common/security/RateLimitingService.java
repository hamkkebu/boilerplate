package com.hamkkebu.boilerplate.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting 서비스 (In-Memory 기반)
 *
 * <p>API 호출 횟수 제한을 통해 DDoS 공격 및 무차별 대입 공격을 방어합니다.</p>
 *
 * <p>Rate Limit 정책:</p>
 * <ul>
 *   <li>인증 API: 5 requests / 1분 (로그인, 회원가입 등)</li>
 *   <li>일반 API: 100 requests / 1분</li>
 * </ul>
 *
 * <p>구현 방식: Sliding Window Counter (ConcurrentHashMap + 만료 시간)</p>
 */
@Slf4j
@Service
public class RateLimitingService {

    private final Map<String, RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();

    @Value("${security.rate-limiting.enabled:false}")
    private boolean enabled;

    @Value("${security.rate-limiting.auth-requests-per-minute:5}")
    private int authRequestsPerMinute;

    @Value("${security.rate-limiting.general-requests-per-minute:100}")
    private int generalRequestsPerMinute;

    @Value("${security.rate-limiting.window-size-seconds:60}")
    private long windowSizeSeconds;

    /**
     * Rate Limit 체크 (인증 API용)
     */
    public boolean tryConsumeAuth(String key) {
        return tryConsume(key, authRequestsPerMinute);
    }

    /**
     * Rate Limit 체크 (일반 API용)
     */
    public boolean tryConsumeGeneral(String key) {
        return tryConsume(key, generalRequestsPerMinute);
    }

    /**
     * Rate Limiting 활성화 여부
     */
    public boolean isEnabled() {
        return enabled;
    }

    private boolean tryConsume(String key, int limit) {
        try {
            long now = System.currentTimeMillis();
            long windowMs = windowSizeSeconds * 1000;

            RateLimitEntry entry = rateLimitMap.compute(key, (k, existing) -> {
                if (existing == null || (now - existing.windowStart) > windowMs) {
                    return new RateLimitEntry(now, new AtomicInteger(1));
                }
                existing.count.incrementAndGet();
                return existing;
            });

            if (entry.count.get() > limit) {
                log.warn("Rate limit exceeded: key={}, count={}, limit={}/min", key, entry.count.get(), limit);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Rate limiting error: key={}, error={}", key, e.getMessage());
            return true; // 에러 시 허용 (가용성 우선)
        }
    }

    /**
     * 특정 키의 Rate Limit 초기화 (테스트용)
     */
    public void reset(String key) {
        rateLimitMap.remove(key);
    }

    private static class RateLimitEntry {
        final long windowStart;
        final AtomicInteger count;

        RateLimitEntry(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
