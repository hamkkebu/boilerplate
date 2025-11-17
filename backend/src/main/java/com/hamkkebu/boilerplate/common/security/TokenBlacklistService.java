package com.hamkkebu.boilerplate.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 토큰 블랙리스트 관리 서비스
 *
 * <p>로그아웃된 토큰을 블랙리스트로 관리하여 재사용을 방지합니다.</p>
 * <p>실제 운영 환경에서는 Redis 등의 외부 저장소 사용을 권장합니다.</p>
 */
@Slf4j
@Service
public class TokenBlacklistService {

    // 메모리 기반 블랙리스트 (실제 운영에서는 Redis 사용 권장)
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * 토큰을 블랙리스트에 추가
     *
     * @param token 블랙리스트에 추가할 토큰
     * @param expirationTime 토큰 만료 시간 (밀리초)
     */
    public void addToBlacklist(String token, long expirationTime) {
        blacklist.put(token, expirationTime);
        log.info("Token added to blacklist. Total blacklisted tokens: {}", blacklist.size());

        // 만료된 토큰 정리
        cleanupExpiredTokens();
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     *
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true, 아니면 false
     */
    public boolean isBlacklisted(String token) {
        Long expirationTime = blacklist.get(token);

        if (expirationTime == null) {
            return false;
        }

        // 이미 만료된 토큰이면 블랙리스트에서 제거
        if (System.currentTimeMillis() > expirationTime) {
            blacklist.remove(token);
            return false;
        }

        return true;
    }

    /**
     * 만료된 토큰을 블랙리스트에서 제거
     */
    private void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        int beforeSize = blacklist.size();

        blacklist.entrySet().removeIf(entry -> currentTime > entry.getValue());

        int removedCount = beforeSize - blacklist.size();
        if (removedCount > 0) {
            log.info("Cleaned up {} expired tokens from blacklist", removedCount);
        }
    }

    /**
     * 블랙리스트 크기 반환 (모니터링용)
     *
     * @return 블랙리스트에 있는 토큰 수
     */
    public int getBlacklistSize() {
        cleanupExpiredTokens();
        return blacklist.size();
    }
}
