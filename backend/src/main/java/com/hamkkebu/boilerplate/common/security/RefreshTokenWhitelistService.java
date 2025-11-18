package com.hamkkebu.boilerplate.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * RefreshToken Whitelist 관리 서비스 (Redis 기반)
 *
 * <p>유효한 refreshToken만 Redis에 저장하여 관리합니다.</p>
 * <p>Blacklist 방식과 달리 명시적으로 허용된 토큰만 사용 가능합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>로그인 시 refreshToken을 Redis에 저장</li>
 *   <li>토큰 갱신 시 Redis에 존재하는지 검증</li>
 *   <li>로그아웃/회원탈퇴 시 Redis에서 삭제</li>
 *   <li>만료 시간 도래 시 Redis에서 자동 삭제 (TTL)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenWhitelistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    /**
     * RefreshToken을 Whitelist에 추가 (Redis 저장)
     *
     * @param userId 사용자 ID
     * @param refreshToken RefreshToken
     * @param expirationMillis 만료 시간 (밀리초)
     */
    public void addToWhitelist(String userId, String refreshToken, long expirationMillis) {
        String key = REFRESH_TOKEN_PREFIX + userId;

        // Redis에 저장 (TTL 자동 설정)
        long ttlSeconds = expirationMillis / 1000;
        redisTemplate.opsForValue().set(key, refreshToken, ttlSeconds, TimeUnit.SECONDS);

        log.info("RefreshToken added to whitelist: userId={}, ttl={}s", userId, ttlSeconds);
    }

    /**
     * RefreshToken이 Whitelist에 있는지 확인
     *
     * @param userId 사용자 ID
     * @param refreshToken 확인할 RefreshToken
     * @return Whitelist에 있고 일치하면 true, 아니면 false
     */
    public boolean isWhitelisted(String userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(key);

        if (storedToken == null) {
            log.warn("RefreshToken not found in whitelist: userId={}", userId);
            return false;
        }

        boolean isValid = storedToken.equals(refreshToken);

        if (!isValid) {
            log.warn("RefreshToken mismatch: userId={}", userId);
        }

        return isValid;
    }

    /**
     * RefreshToken을 Whitelist에서 제거 (로그아웃/회원탈퇴)
     *
     * @param userId 사용자 ID
     */
    public void removeFromWhitelist(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        Boolean deleted = redisTemplate.delete(key);

        if (Boolean.TRUE.equals(deleted)) {
            log.info("RefreshToken removed from whitelist: userId={}", userId);
        } else {
            log.warn("RefreshToken not found in whitelist for removal: userId={}", userId);
        }
    }

    /**
     * 특정 사용자의 RefreshToken 조회
     *
     * @param userId 사용자 ID
     * @return RefreshToken (없으면 null)
     */
    public String getRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Whitelist에 저장된 토큰 개수 (모니터링용)
     *
     * @return 토큰 개수
     */
    public long getWhitelistSize() {
        return redisTemplate.keys(REFRESH_TOKEN_PREFIX + "*").size();
    }
}
