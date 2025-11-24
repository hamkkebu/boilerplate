package com.hamkkebu.boilerplate.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
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
     * <p>Redis 연결 실패 시 예외를 발생시킵니다 (로그인 실패로 처리).</p>
     *
     * @param userId 사용자 ID
     * @param refreshToken RefreshToken
     * @param expirationMillis 만료 시간 (밀리초)
     * @throws RedisConnectionFailureException Redis 연결 실패 시
     */
    public void addToWhitelist(String userId, String refreshToken, long expirationMillis) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;

            // Redis에 저장 (TTL 자동 설정)
            long ttlSeconds = expirationMillis / 1000;
            redisTemplate.opsForValue().set(key, refreshToken, ttlSeconds, TimeUnit.SECONDS);

            log.info("RefreshToken added to whitelist: userId={}, ttl={}s", userId, ttlSeconds);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failed while adding refresh token: userId={}, error={}",
                userId, e.getMessage());
            // 로그인 시에는 Redis 실패를 에러로 처리 (토큰 저장 실패)
            throw e;
        }
    }

    /**
     * RefreshToken이 Whitelist에 있는지 확인
     *
     * <p>Redis 연결 실패 시 false를 반환합니다 (보안 우선).</p>
     *
     * @param userId 사용자 ID
     * @param refreshToken 확인할 RefreshToken
     * @return Whitelist에 있고 일치하면 true, 아니면 false
     */
    public boolean isWhitelisted(String userId, String refreshToken) {
        try {
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
        } catch (RedisConnectionFailureException e) {
            // Redis 연결 실패 시 false 반환 (보안 우선)
            log.warn("Redis connection failed while checking whitelist: userId={}, error={}",
                userId, e.getMessage());
            return false;
        }
    }

    /**
     * RefreshToken을 Whitelist에서 제거 (로그아웃/회원탈퇴)
     *
     * <p>Redis 연결 실패 시 로그만 남기고 계속 진행합니다 (가용성 우선).</p>
     * <p>회원 탈퇴/로그아웃 프로세스는 Redis 장애와 무관하게 완료되어야 합니다.</p>
     *
     * @param userId 사용자 ID
     */
    public void removeFromWhitelist(String userId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            Boolean deleted = redisTemplate.delete(key);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("RefreshToken removed from whitelist: userId={}", userId);
            } else {
                log.warn("RefreshToken not found in whitelist for removal: userId={}", userId);
            }
        } catch (RedisConnectionFailureException e) {
            // Redis 연결 실패 시 로그만 남기고 계속 진행 (가용성 우선)
            // 회원 탈퇴/로그아웃은 Redis 장애와 무관하게 완료되어야 함
            log.warn("Redis connection failed while removing refresh token: userId={}, error={}",
                userId, e.getMessage());
        }
    }

    /**
     * 특정 사용자의 RefreshToken 조회
     *
     * <p>Redis 연결 실패 시 null을 반환합니다.</p>
     *
     * @param userId 사용자 ID
     * @return RefreshToken (없으면 null)
     */
    public String getRefreshToken(String userId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            return redisTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed while getting refresh token: userId={}, error={}",
                userId, e.getMessage());
            return null;
        }
    }

    /**
     * REMOVED: getWhitelistSize() 메서드
     *
     * 보안 및 성능상의 이유로 제거됨:
     * - Redis KEYS 명령어는 blocking operation으로 성능 문제 유발
     * - 프로덕션 환경에서 대량의 키가 있을 경우 Redis 서버에 심각한 영향
     * - 모니터링이 필요한 경우 별도의 카운터나 Redis SCAN 명령어 사용 권장
     */
}
