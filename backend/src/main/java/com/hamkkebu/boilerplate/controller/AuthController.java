package com.hamkkebu.boilerplate.controller;

import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import com.hamkkebu.boilerplate.data.dto.LoginRequest;
import com.hamkkebu.boilerplate.data.dto.LoginResponse;
import com.hamkkebu.boilerplate.data.dto.TokenResponse;
import com.hamkkebu.boilerplate.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 *
 * <p>로그인, 토큰 갱신 등 인증 관련 엔드포인트를 제공합니다.</p>
 * <p>보안 전략: RefreshToken Whitelist (Redis 기반)</p>
 */
@Tag(name = "Auth API", description = "인증 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     *
     * <p>로그인 성공 시 accessToken과 refreshToken을 JSON으로 반환합니다.</p>
     * <p>refreshToken은 Redis Whitelist에 저장됩니다.</p>
     *
     * @param request 로그인 요청 (사용자 ID, 비밀번호)
     * @return 로그인 응답 (사용자 정보 + JWT 토큰)
     */
    @Operation(
        summary = "로그인",
        description = "사용자 ID와 비밀번호로 로그인하고 JWT 토큰을 발급받습니다. refreshToken은 Redis Whitelist에 저장됩니다.",
        security = {} // 인증 불필요
    )
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for user: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response, "로그인 성공");
    }

    /**
     * 토큰 갱신
     *
     * <p>refreshToken으로 새로운 accessToken을 발급받습니다.</p>
     * <p>Redis Whitelist에서 refreshToken을 검증합니다.</p>
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰
     */
    @Operation(
        summary = "토큰 갱신",
        description = "refreshToken으로 새로운 accessToken을 발급받습니다. refreshToken은 Redis Whitelist에서 검증됩니다.",
        security = {} // 인증 불필요 (refreshToken 사용)
    )
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        log.info("Token refresh request");
        TokenResponse response = authService.refresh(refreshToken);
        return ApiResponse.success(response, "토큰 갱신 성공");
    }

    /**
     * 토큰 검증
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰 유효 여부
     */
    @Operation(
        summary = "토큰 검증",
        description = "JWT 토큰의 유효성을 검증합니다.",
        security = {} // 인증 불필요 (검증 대상 토큰을 직접 전달)
    )
    @GetMapping("/validate")
    public ApiResponse<Boolean> validateToken(@RequestHeader(value = "Authorization", required = false) String bearerToken) {
        // 토큰이 없는 경우
        if (bearerToken == null || bearerToken.isEmpty()) {
            return ApiResponse.success(false, "토큰이 제공되지 않았습니다");
        }

        // "Bearer " 접두사 제거
        String jwtToken = com.hamkkebu.boilerplate.common.security.JwtTokenProvider.extractToken(bearerToken);
        if (jwtToken == null) {
            jwtToken = bearerToken; // Bearer 없이 직접 토큰만 전달된 경우
        }
        boolean isValid = authService.validateToken(jwtToken);
        return ApiResponse.success(isValid, isValid ? "유효한 토큰입니다" : "유효하지 않은 토큰입니다");
    }

    /**
     * 현재 사용자 정보 조회
     *
     * <p>Spring Security의 인증 정보에서 현재 사용자 ID를 반환합니다.</p>
     * <p>Swagger의 Authorize 버튼으로 설정한 JWT 토큰이 자동으로 사용됩니다.</p>
     *
     * @param authentication Spring Security 인증 정보
     * @return 사용자 ID
     */
    @Operation(summary = "현재 사용자 정보", description = "JWT 토큰에서 현재 사용자 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<String> getCurrentUser(Authentication authentication) {
        String userId = authentication.getName();
        return ApiResponse.success(userId, "사용자 정보 조회 성공");
    }

    /**
     * 로그아웃
     *
     * <p>accessToken으로 사용자를 인증하고, refreshToken을 Redis Whitelist에서 제거합니다.</p>
     * <p>Swagger의 Authorize 버튼으로 설정한 JWT 토큰이 자동으로 사용됩니다.</p>
     *
     * @param authentication Spring Security 인증 정보
     * @param refreshToken 제거할 리프레시 토큰 (선택)
     * @return 로그아웃 성공 메시지
     */
    @Operation(
        summary = "로그아웃",
        description = "로그아웃하고 refreshToken을 Whitelist에서 제거합니다. Refresh-Token 헤더로 제거할 토큰을 전달합니다."
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            Authentication authentication,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken
    ) {
        String userId = authentication.getName();
        log.info("Logout request for user: {}", userId);
        authService.logout(refreshToken);
        return ApiResponse.success("로그아웃 성공");
    }
}
