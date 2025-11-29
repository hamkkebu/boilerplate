package com.hamkkebu.boilerplate.controller;

import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 *
 * <p>Keycloak SSO 기반 인증 정보 조회 API를 제공합니다.</p>
 * <p>로그인/로그아웃은 Keycloak에서 처리됩니다.</p>
 */
@Tag(name = "Auth API", description = "인증 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    /**
     * 현재 사용자 정보 조회
     *
     * <p>Keycloak JWT 토큰에서 현재 사용자 정보를 반환합니다.</p>
     *
     * @param jwt Keycloak JWT 토큰
     * @return 사용자 ID (sub claim)
     */
    @Operation(summary = "현재 사용자 정보", description = "Keycloak JWT 토큰에서 현재 사용자 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<String> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ApiResponse.success(userId, "사용자 정보 조회 성공");
    }
}
