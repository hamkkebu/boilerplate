package com.hamkkebu.authservice.service;

import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import com.hamkkebu.boilerplate.common.security.JwtTokenProvider;
import com.hamkkebu.boilerplate.common.security.LoginAttemptService;
import com.hamkkebu.boilerplate.common.security.PasswordValidator;
import com.hamkkebu.boilerplate.common.security.RefreshTokenWhitelistService;
import com.hamkkebu.authservice.data.dto.LoginRequest;
import com.hamkkebu.authservice.data.dto.LoginResponse;
import com.hamkkebu.authservice.data.dto.UserResponse;
import com.hamkkebu.authservice.data.dto.TokenResponse;
import com.hamkkebu.authservice.data.entity.User;
import com.hamkkebu.authservice.data.mapper.UserMapper;
import com.hamkkebu.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>로그인 처리 및 JWT 토큰 발급</li>
 *   <li>리프레시 토큰으로 액세스 토큰 갱신</li>
 *   <li>RefreshToken Whitelist 관리 (Redis)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenWhitelistService refreshTokenWhitelistService;
    private final LoginAttemptService loginAttemptService;

    @Value("${jwt.refresh-token-validity:#{7 * 24 * 60 * 60 * 1000}}") // 7일 (밀리초)
    private long refreshTokenValidity;

    // SECURITY: Timing Attack 방어를 위한 더미 비밀번호 해시 (BCrypt 12 rounds)
    private static final String DUMMY_PASSWORD_HASH =
            "$2a$12$dummyHashToPreventTimingAttack1234567890123456789012";

    /**
     * 로그인 처리
     *
     * SECURITY 강화:
     * - 로그인 실패 횟수 제한 (5회)
     * - Timing Attack 방어 (더미 비밀번호 검증)
     * - User Enumeration 방지 (동일한 에러 메시지)
     *
     * @param request 로그인 요청 (사용자 ID, 비밀번호)
     * @return 로그인 응답 (사용자 정보 + JWT 토큰)
     * @throws BusinessException 사용자를 찾을 수 없거나 비밀번호가 일치하지 않는 경우
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // SECURITY: 로그인 차단 여부 확인
        if (loginAttemptService.isBlocked(request.getUsername())) {
            log.warn("Login blocked due to too many attempts: {}", request.getUsername());
            throw new BusinessException(
                ErrorCode.AUTHENTICATION_FAILED,
                "로그인 시도 횟수가 초과되었습니다. 15분 후 다시 시도하세요."
            );
        }

        // SECURITY: Timing Attack 방어 - 항상 동일한 시간 소요
        // 사용자가 존재하지 않아도 비밀번호 검증을 수행하여 시간 차이를 감춤
        User user = userRepository.findByUsernameAndIsDeletedFalse(request.getUsername())
            .orElse(null);

        boolean userExists = (user != null);
        boolean passwordMatches = false;

        if (userExists) {
            // 실제 사용자: 실제 비밀번호 검증
            passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        } else {
            // SECURITY: Timing Attack 방어
            // 더미 비밀번호 해시와 비교하여 실행 시간 동일하게 유지
            passwordEncoder.matches(request.getPassword(), DUMMY_PASSWORD_HASH);
        }

        // 로그인 실패 처리
        if (!userExists || !passwordMatches) {
            loginAttemptService.recordLoginAttempt(request.getUsername());
            int remaining = loginAttemptService.getRemainingAttempts(request.getUsername());

            log.warn("Login failed: userId={}, userExists={}, remaining={}",
                request.getUsername(), userExists, remaining);

            // 사용자가 존재하지 않는 경우 (회원가입 유도를 위해 별도 에러 코드 사용)
            if (!userExists) {
                throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND,
                    "등록되지 않은 아이디입니다. 회원가입을 진행해주세요."
                );
            }

            // 비밀번호 불일치
            throw new BusinessException(
                ErrorCode.AUTHENTICATION_FAILED,
                String.format("비밀번호가 일치하지 않습니다. (남은 시도: %d회)", remaining)
            );
        }

        // 로그인 성공: 시도 횟수 초기화
        loginAttemptService.resetLoginAttempts(request.getUsername());

        // SECURITY: 마지막 로그인 시간 업데이트 (Audit Trail)
        user.updateLastLoginAt();
        userRepository.save(user);

        // JWT 토큰 생성 (RBAC: 사용자 권한 포함)
        String role = user.getRole().getAuthority();
        String accessToken = jwtTokenProvider.createAccessToken(user.getUsername(), role);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername(), role);

        // RefreshToken을 Redis Whitelist에 저장
        refreshTokenWhitelistService.addToWhitelist(
            user.getUsername(),
            refreshToken,
            refreshTokenValidity
        );

        // 토큰 만료 시간 (밀리초 -> 초)
        Long expiresIn = (jwtTokenProvider.getExpirationDate(accessToken).getTime() - System.currentTimeMillis()) / 1000;

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, expiresIn);
        UserResponse responseUser = userMapper.toDto(user);

        log.info("Login successful for user: {}", request.getUsername());

        return LoginResponse.of(responseUser, tokenResponse);
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     *
     * SECURITY: Refresh Token Rotation 구현
     * - 기존 Refresh Token 무효화
     * - 새로운 Refresh Token 발급
     * - 탈취된 토큰 재사용 방지
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰 + 새로운 리프레시 토큰
     * @throws BusinessException 토큰이 유효하지 않은 경우
     */
    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        log.info("Token refresh attempt");

        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("Invalid refresh token");
            throw new BusinessException(
                ErrorCode.INVALID_REFRESH_TOKEN,
                "유효하지 않은 리프레시 토큰입니다"
            );
        }

        // 사용자 ID 추출
        String userId = jwtTokenProvider.getUserId(refreshToken);

        // Redis Whitelist에서 refreshToken 검증
        if (!refreshTokenWhitelistService.isWhitelisted(userId, refreshToken)) {
            log.warn("RefreshToken not in whitelist: userId={}", userId);
            throw new BusinessException(
                ErrorCode.INVALID_REFRESH_TOKEN,
                "유효하지 않거나 로그아웃된 리프레시 토큰입니다. 다시 로그인해주세요."
            );
        }

        // 사용자 존재 여부 확인
        User user = userRepository.findByUsernameAndIsDeletedFalse(userId)
            .orElseThrow(() -> {
                log.warn("User not found during token refresh: {}", userId);
                return new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "사용자를 찾을 수 없습니다"
                );
            });

        // SECURITY: Refresh Token Rotation
        // RBAC: 사용자 권한 포함
        String role = user.getRole().getAuthority();

        // 1. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, role);

        // 2. 새로운 Refresh Token 생성
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, role);

        // 3. 기존 Refresh Token 무효화
        refreshTokenWhitelistService.removeFromWhitelist(userId);

        // 4. 새로운 Refresh Token을 Whitelist에 저장
        refreshTokenWhitelistService.addToWhitelist(
            userId,
            newRefreshToken,
            refreshTokenValidity
        );

        Long expiresIn = (jwtTokenProvider.getExpirationDate(newAccessToken).getTime() - System.currentTimeMillis()) / 1000;

        log.info("Token refresh successful with rotation: userId={}", userId);

        // SECURITY: 새로운 Refresh Token도 함께 반환
        return TokenResponse.of(newAccessToken, newRefreshToken, expiresIn);
    }

    /**
     * 토큰 검증
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 토큰에서 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     * @throws BusinessException 토큰이 유효하지 않은 경우
     */
    public String getUserIdFromToken(String token) {
        try {
            return jwtTokenProvider.getUserId(token);
        } catch (Exception e) {
            log.warn("Failed to extract user ID from token: {}", e.getMessage());
            throw new BusinessException(
                ErrorCode.INVALID_TOKEN,
                "유효하지 않은 토큰입니다"
            );
        }
    }

    /**
     * 로그아웃 처리
     *
     * <p>refreshToken을 Redis Whitelist에서 제거합니다.</p>
     *
     * @param refreshToken 리프레시 토큰
     */
    public void logout(String refreshToken) {
        log.info("Logout attempt");

        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Logout called without refreshToken");
            return;
        }

        try {
            // refreshToken에서 사용자 ID 추출
            String userId = jwtTokenProvider.getUserId(refreshToken);

            // Redis Whitelist에서 제거
            refreshTokenWhitelistService.removeFromWhitelist(userId);

            log.info("Logout successful for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to logout", e);
            // 로그아웃 실패해도 클라이언트에서 토큰을 삭제하므로 문제없음
        }
    }
}
