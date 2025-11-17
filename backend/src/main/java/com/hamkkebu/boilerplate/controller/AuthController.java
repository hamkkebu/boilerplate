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
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 *
 * <p>로그인, 토큰 갱신 등 인증 관련 엔드포인트를 제공합니다.</p>
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
     * @param request 로그인 요청 (사용자 ID, 비밀번호)
     * @return 로그인 응답 (사용자 정보 + JWT 토큰)
     */
    @Operation(summary = "로그인", description = "사용자 ID와 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for user: {}", request.getSampleId());
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response, "로그인 성공");
    }

    /**
     * 토큰 갱신
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰
     */
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다.")
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
    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증합니다.")
    @GetMapping("/validate")
    public ApiResponse<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        // "Bearer " 접두사 제거
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        boolean isValid = authService.validateToken(jwtToken);
        return ApiResponse.success(isValid, isValid ? "유효한 토큰입니다" : "유효하지 않은 토큰입니다");
    }

    /**
     * 현재 사용자 정보 조회
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    @Operation(summary = "현재 사용자 정보", description = "JWT 토큰에서 현재 사용자 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<String> getCurrentUser(@RequestHeader("Authorization") String token) {
        // "Bearer " 접두사 제거
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        String userId = authService.getUserIdFromToken(jwtToken);
        return ApiResponse.success(userId, "사용자 정보 조회 성공");
    }

    /**
     * 로그아웃
     *
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @return 로그아웃 성공 메시지
     */
    @Operation(summary = "로그아웃", description = "로그아웃하고 토큰을 무효화합니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        log.info("Logout request");

        // "Bearer " 접두사 제거
        String jwtAccessToken = null;
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            jwtAccessToken = accessToken.substring(7);
        }

        authService.logout(jwtAccessToken, refreshToken);
        return ApiResponse.success("로그아웃 성공");
    }
}
