package com.hamkkebu.boilerplate.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Rate Limiting Interceptor
 *
 * <p>모든 API 요청에 대해 Rate Limit을 적용합니다.</p>
 *
 * <p>사용자 식별:</p>
 * <ul>
 *   <li>인증된 사용자: userId 사용</li>
 *   <li>비인증 사용자: IP 주소 사용</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;

    /**
     * SECURITY: 프록시 사용 여부
     * 프록시가 없는 환경에서는 false로 설정하여 X-Forwarded-For 헤더 무시
     * 프로덕션에서는 application.yml에서 설정
     */
    @org.springframework.beans.factory.annotation.Value("${security.rate-limiting.trust-proxy:false}")
    private boolean trustProxy;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        // Rate Limiting이 비활성화된 경우 통과
        if (!rateLimitingService.isEnabled()) {
            return true;
        }

        String userKey = getUserKey(request);

        // 인증 API 체크
        boolean allowed;
        if (isAuthEndpoint(request)) {
            allowed = rateLimitingService.tryConsumeAuth(userKey);
        } else {
            allowed = rateLimitingService.tryConsumeGeneral(userKey);
        }

        if (!allowed) {
            handleRateLimitExceeded(request, response);
            return false;
        }

        return true;
    }

    /**
     * 사용자 식별 키 생성
     *
     * @param request HTTP 요청
     * @return 사용자 식별 키 (userId 또는 IP)
     */
    private String getUserKey(HttpServletRequest request) {
        // 인증된 사용자: userId 사용
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }

        // 비인증 사용자: IP 주소 사용
        String ip = getClientIP(request);
        return "ip:" + ip;
    }

    /**
     * 클라이언트 IP 주소 추출
     *
     * <p>SECURITY: IP 스푸핑 방어</p>
     * <ul>
     *   <li>trustProxy=false: request.getRemoteAddr() 직접 사용 (안전)</li>
     *   <li>trustProxy=true: X-Forwarded-For 헤더 확인 (프록시 환경)</li>
     * </ul>
     *
     * @param request HTTP 요청
     * @return 클라이언트 IP 주소
     */
    private String getClientIP(HttpServletRequest request) {
        // SECURITY: 프록시를 신뢰하지 않는 경우 getRemoteAddr() 직접 사용
        if (!trustProxy) {
            String ip = request.getRemoteAddr();
            log.debug("Client IP (direct): {}", ip);
            return ip;
        }

        // 프록시를 신뢰하는 경우에만 X-Forwarded-For 등의 헤더 확인
        String[] headerNames = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (isValidIP(ip)) {
                // 여러 IP가 있는 경우 첫 번째 IP 사용 (실제 클라이언트 IP)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                log.debug("Client IP (via proxy, header={}): {}", headerName, ip);
                return ip;
            }
        }

        // 모든 헤더에서 IP를 찾지 못한 경우 getRemoteAddr() 사용
        String ip = request.getRemoteAddr();
        log.debug("Client IP (fallback): {}", ip);
        return ip;
    }

    /**
     * IP 주소 유효성 검증
     *
     * @param ip IP 주소
     * @return 유효하면 true
     */
    private boolean isValidIP(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 인증 엔드포인트 체크
     *
     * <p>다음 엔드포인트들은 더 엄격한 Rate Limiting 적용:</p>
     * <ul>
     *   <li>로그인: /api/v1/auth/login</li>
     *   <li>토큰 갱신: /api/v1/auth/refresh</li>
     *   <li>중복 확인: /api/v1/users/check/**</li>
     *   <li>회원가입: POST /api/v1/users</li>
     * </ul>
     *
     * @param request HTTP 요청
     * @return 인증 엔드포인트이면 true
     */
    private boolean isAuthEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/refresh")
                || path.startsWith("/api/v1/users/check/")
                || (path.equals("/api/v1/users") && "POST".equalsIgnoreCase(method)); // 회원가입
    }

    /**
     * Rate Limit 초과 시 429 응답 반환
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     */
    private void handleRateLimitExceeded(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        ErrorCode errorCode = ErrorCode.RATE_LIMIT_EXCEEDED;

        response.setStatus(errorCode.getStatusValue()); // 429
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Retry-After 헤더 추가 (60초 후 재시도)
        response.setHeader("Retry-After", "60");

        ApiResponse<Void> apiResponse = ApiResponse.error(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        log.warn("Rate limit exceeded: method={}, uri={}, ip={}",
                request.getMethod(),
                request.getRequestURI(),
                getClientIP(request)
        );
    }
}
