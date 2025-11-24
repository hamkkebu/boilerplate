package com.hamkkebu.boilerplate.common.security;

import com.hamkkebu.boilerplate.common.constant.CommonConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

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
     * SecretKey 초기화 및 검증
     *
     * <p>SECURITY: JWT Secret 길이 검증 (최소 256-bit / 32 bytes)</p>
     * <p>이 메서드는 @PostConstruct로 Bean 초기화 시점에 실행되어
     * 애플리케이션 시작 전에 JWT Secret의 유효성을 검증합니다.</p>
     * <p>검증 실패 시 애플리케이션이 시작되지 않습니다.</p>
     */
    @PostConstruct
    protected void init() {
        log.info("Validating JWT_SECRET configuration...");

        // 환경 변수가 설정되지 않았거나 비어있는 경우
        if (secretKey == null || secretKey.trim().isEmpty()) {
            String errorMsg = """

                ================================================================================
                CONFIGURATION ERROR: JWT_SECRET is not configured!
                ================================================================================
                Environment variable 'JWT_SECRET' is required but not set.

                Please set JWT_SECRET in your .env file or environment variables:
                  export JWT_SECRET=$(openssl rand -base64 32)

                For production, use at least 512-bit (64 bytes):
                  export JWT_SECRET=$(openssl rand -base64 64)
                ================================================================================
                """;
            log.error(errorMsg);
            throw new IllegalStateException("JWT_SECRET environment variable is not configured");
        }

        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        // SECURITY: Secret 길이 검증 (최소 256-bit = 32 bytes)
        if (keyBytes.length < 32) {
            String errorMsg = String.format("""

                ================================================================================
                SECURITY ERROR: JWT_SECRET is too short!
                ================================================================================
                JWT_SECRET must be at least 256 bits (32 bytes).
                Current length: %d bytes

                Generate a secure secret key:
                  openssl rand -base64 32   # For 256-bit security (minimum)
                  openssl rand -base64 64   # For 512-bit security (recommended)

                Set it in your .env file:
                  JWT_SECRET=<generated_secret_here>
                ================================================================================
                """,
                keyBytes.length
            );
            log.error(errorMsg);
            throw new IllegalStateException(String.format(
                "JWT_SECRET is too short (%d bytes). Minimum required: 32 bytes",
                keyBytes.length
            ));
        }

        // SecretKey 생성
        this.key = Keys.hmacShaKeyFor(keyBytes);

        log.info("✓ JWT_SECRET validation successful");
        log.info("✓ JWT SecretKey initialized (length: {} bytes, security: {}-bit)",
            keyBytes.length, keyBytes.length * 8);
        log.info("✓ Access token validity: {} ms ({} hour)", accessTokenValidity, accessTokenValidity / 3600000);
        log.info("✓ Refresh token validity: {} ms ({} days)", refreshTokenValidity, refreshTokenValidity / 86400000);
    }

    /**
     * 액세스 토큰 생성
     *
     * @param userId 사용자 ID
     * @param role 사용자 권한 (RBAC)
     * @return JWT 액세스 토큰
     */
    public String createAccessToken(String userId, String role) {
        return createToken(userId, role, accessTokenValidity);
    }

    /**
     * 리프레시 토큰 생성
     *
     * @param userId 사용자 ID
     * @param role 사용자 권한 (RBAC)
     * @return JWT 리프레시 토큰
     */
    public String createRefreshToken(String userId, String role) {
        return createToken(userId, role, refreshTokenValidity);
    }

    /**
     * JWT 토큰 생성
     *
     * SECURITY 강화:
     * - JTI (JWT ID) 추가: 개별 토큰 추적 및 무효화 가능
     * - 알고리즘 명시 (HS512): "none" 알고리즘 공격 방지
     * - RBAC: 사용자 권한 정보 포함
     *
     * @param userId   사용자 ID
     * @param role     사용자 권한 (RBAC)
     * @param validity 토큰 유효 기간 (밀리초)
     * @return JWT 토큰
     */
    private String createToken(String userId, String role, long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);

        // SECURITY: JTI (JWT ID) 생성 - 토큰 추적/무효화에 사용 가능
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                // JTI (JWT ID) - 토큰 고유 식별자
                .id(jti)
                // Subject - 사용자 ID
                .subject(userId)
                // RBAC: 사용자 권한
                .claim("role", role)
                // Issued At - 발급 시간
                .issuedAt(now)
                // Expiration - 만료 시간
                .expiration(expiryDate)
                // SECURITY: 알고리즘 명시 (HS512)
                // - HS512: HMAC-SHA512 (512-bit)
                // - "none" 알고리즘 공격 방지
                .signWith(key, Jwts.SIG.HS512)
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
     * 토큰에서 사용자 권한(Role) 추출
     *
     * @param token JWT 토큰
     * @return 사용자 권한 (ROLE_USER, ROLE_ADMIN, ROLE_DEVELOPER)
     */
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
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

    /**
     * Bearer 토큰에서 JWT 토큰 추출
     *
     * <p>"Bearer " 접두사를 제거하고 순수 JWT 토큰만 반환합니다.</p>
     *
     * @param bearerToken "Bearer {token}" 형식의 토큰
     * @return JWT 토큰 (Bearer 접두사가 없으면 null 반환)
     */
    public static String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(CommonConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(CommonConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * Bearer 토큰 형식으로 변환
     *
     * @param token JWT 토큰
     * @return "Bearer {token}" 형식의 토큰
     */
    public static String toBearerToken(String token) {
        if (token == null) {
            return null;
        }
        return CommonConstants.TOKEN_PREFIX + token;
    }
}
