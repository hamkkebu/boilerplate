package com.hamkkebu.boilerplate.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 Provider
 *
 * <p>JWT(JSON Web Token)를 사용하여 사용자 인증을 처리합니다.</p>
 * <p>주요 기능:</p>
 * <ul>
 *   <li>액세스 토큰 생성</li>
 *   <li>리프레시 토큰 생성</li>
 *   <li>토큰 검증 및 파싱</li>
 *   <li>사용자 ID 추출</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity:3600000}") // 1시간 (밀리초)
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity:604800000}") // 7일 (밀리초)
    private long refreshTokenValidity;

    private SecretKey key;

    /**
     * SecretKey 초기화
     */
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        log.info("JWT SecretKey initialized");
    }

    /**
     * 액세스 토큰 생성
     *
     * @param userId 사용자 ID
     * @return JWT 액세스 토큰
     */
    public String createAccessToken(String userId) {
        return createToken(userId, accessTokenValidity);
    }

    /**
     * 리프레시 토큰 생성
     *
     * @param userId 사용자 ID
     * @return JWT 리프레시 토큰
     */
    public String createRefreshToken(String userId) {
        return createToken(userId, refreshTokenValidity);
    }

    /**
     * JWT 토큰 생성
     *
     * @param userId   사용자 ID
     * @param validity 토큰 유효 기간 (밀리초)
     * @return JWT 토큰
     */
    private String createToken(String userId, long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT token validation error: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰 파싱 및 Claims 추출
     *
     * @param token JWT 토큰
     * @return Claims 객체
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰 만료 여부 확인
     *
     * @param token JWT 토큰
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 토큰 만료 시간 조회
     *
     * @param token JWT 토큰
     * @return 만료 시간 (Date)
     */
    public Date getExpirationDate(String token) {
        return parseClaims(token).getExpiration();
    }
}
