package com.hamkkebu.boilerplate.service;

import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import com.hamkkebu.boilerplate.common.security.JwtTokenProvider;
import com.hamkkebu.boilerplate.common.security.TokenBlacklistService;
import com.hamkkebu.boilerplate.data.dto.LoginRequest;
import com.hamkkebu.boilerplate.data.dto.LoginResponse;
import com.hamkkebu.boilerplate.data.dto.ResponseSample;
import com.hamkkebu.boilerplate.data.dto.TokenResponse;
import com.hamkkebu.boilerplate.data.entity.Sample;
import com.hamkkebu.boilerplate.data.mapper.SampleMapper;
import com.hamkkebu.boilerplate.repository.SampleJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 *   <li>비밀번호 검증</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SampleJpaRepository sampleRepository;
    private final SampleMapper sampleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 로그인 처리
     *
     * @param request 로그인 요청 (사용자 ID, 비밀번호)
     * @return 로그인 응답 (사용자 정보 + JWT 토큰)
     * @throws BusinessException 사용자를 찾을 수 없거나 비밀번호가 일치하지 않는 경우
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getSampleId());

        // 사용자 조회
        Sample sample = sampleRepository.findBySampleIdAndDeletedFalse(request.getSampleId())
            .orElseThrow(() -> {
                log.warn("User not found: {}", request.getSampleId());
                return new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "사용자를 찾을 수 없습니다"
                );
            });

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), sample.getSamplePassword())) {
            log.warn("Invalid password for user: {}", request.getSampleId());
            throw new BusinessException(
                ErrorCode.AUTHENTICATION_FAILED,
                "비밀번호가 일치하지 않습니다"
            );
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(sample.getSampleId());
        String refreshToken = jwtTokenProvider.createRefreshToken(sample.getSampleId());

        // 토큰 만료 시간 (밀리초 -> 초)
        Long expiresIn = (jwtTokenProvider.getExpirationDate(accessToken).getTime() - System.currentTimeMillis()) / 1000;

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, expiresIn);
        ResponseSample responseSample = sampleMapper.toDto(sample);

        log.info("Login successful for user: {}", request.getSampleId());

        return LoginResponse.of(responseSample, tokenResponse);
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰
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

        // 사용자 존재 여부 확인
        sampleRepository.findBySampleIdAndDeletedFalse(userId)
            .orElseThrow(() -> {
                log.warn("User not found during token refresh: {}", userId);
                return new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "사용자를 찾을 수 없습니다"
                );
            });

        // 새로운 액세스 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        Long expiresIn = (jwtTokenProvider.getExpirationDate(newAccessToken).getTime() - System.currentTimeMillis()) / 1000;

        log.info("Token refresh successful for user: {}", userId);

        return TokenResponse.of(newAccessToken, refreshToken, expiresIn);
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
     */
    public String getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserId(token);
    }

    /**
     * 로그아웃 처리
     *
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     */
    public void logout(String accessToken, String refreshToken) {
        log.info("Logout attempt");

        // 액세스 토큰 블랙리스트 추가
        if (accessToken != null && !accessToken.isEmpty()) {
            long accessTokenExpiration = jwtTokenProvider.getExpirationDate(accessToken).getTime();
            tokenBlacklistService.addToBlacklist(accessToken, accessTokenExpiration);
        }

        // 리프레시 토큰 블랙리스트 추가
        if (refreshToken != null && !refreshToken.isEmpty()) {
            long refreshTokenExpiration = jwtTokenProvider.getExpirationDate(refreshToken).getTime();
            tokenBlacklistService.addToBlacklist(refreshToken, refreshTokenExpiration);
        }

        log.info("Logout successful. Tokens added to blacklist");
    }
}
