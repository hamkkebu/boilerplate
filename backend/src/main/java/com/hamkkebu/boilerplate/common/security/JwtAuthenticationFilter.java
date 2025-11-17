package com.hamkkebu.boilerplate.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 인증 필터
 *
 * <p>HTTP 요청에서 JWT 토큰을 추출하고 검증하여 인증 정보를 SecurityContext에 설정합니다.</p>
 * <p>주요 기능:</p>
 * <ul>
 *   <li>Authorization 헤더에서 Bearer 토큰 추출</li>
 *   <li>JWT 토큰 유효성 검증</li>
 *   <li>인증 정보를 Spring Security Context에 설정</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * JWT 토큰을 검증하고 인증 정보를 설정하는 필터
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Request에서 JWT 토큰 추출
            String token = extractToken(request);

            // 2. 토큰 블랙리스트 확인
            if (StringUtils.hasText(token) && tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Blocked request with blacklisted token");
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 토큰 유효성 검증 및 인증 정보 설정
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                // 토큰에서 사용자 ID 추출
                String userId = jwtTokenProvider.getUserId(token);

                // 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.emptyList() // 권한 정보 (필요시 추가)
                        );

                // 인증 상세 정보 설정
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Set authentication for user: {}", userId);
            }
        } catch (Exception e) {
            log.error("Failed to set user authentication: {}", e.getMessage());
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰 추출
     *
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * 특정 경로에 대해 필터를 적용하지 않을 경우 오버라이드
     * (현재는 모든 요청에 대해 필터 적용)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 필요시 특정 경로 제외
        // String path = request.getRequestURI();
        // return path.startsWith("/api/v1/auth/");
        return false;
    }
}
