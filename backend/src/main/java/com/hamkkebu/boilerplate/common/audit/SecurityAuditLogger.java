package com.hamkkebu.boilerplate.common.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 보안 감사 로그 시스템 (AOP 기반)
 *
 * <p>중요한 보안 이벤트를 자동으로 로깅합니다.</p>
 *
 * <p>로깅 대상 이벤트:</p>
 * <ul>
 *   <li>Rate Limit 초과</li>
 *   <li>권한 거부 (ACCESS_DENIED)</li>
 * </ul>
 *
 * <p>로그 포맷:</p>
 * <pre>
 * [SECURITY_AUDIT] timestamp | event | userId | ip | status | details
 * </pre>
 *
 * <p>Note: 로그인/로그아웃/토큰 갱신은 Keycloak에서 처리합니다.</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAuditLogger {

    /**
     * Rate Limit 초과 로깅
     */
    @AfterReturning(
            pointcut = "execution(* com.hamkkebu.boilerplate.common.security.RateLimitingService.tryConsume*(..))",
            returning = "allowed"
    )
    public void logRateLimit(JoinPoint joinPoint, boolean allowed) {
        if (!allowed) {
            String key = (String) joinPoint.getArgs()[0];
            String ip = getClientIP();

            log.warn("[SECURITY_AUDIT] {} | EVENT=RATE_LIMIT_EXCEEDED | KEY={} | IP={} | STATUS=BLOCKED",
                    LocalDateTime.now(ZoneId.systemDefault()), key, ip);
        }
    }

    /**
     * 권한 거부 로깅
     */
    @AfterThrowing(
            pointcut = "execution(* com.hamkkebu..controller..*(..))",
            throwing = "ex"
    )
    public void logAccessDenied(JoinPoint joinPoint, Exception ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("권한")) {
            String userId = getCurrentUserId();
            String ip = getClientIP();
            String method = joinPoint.getSignature().toShortString();

            log.warn("[SECURITY_AUDIT] {} | EVENT=ACCESS_DENIED | USER={} | IP={} | METHOD={} | STATUS=DENIED | REASON={}",
                    LocalDateTime.now(ZoneId.systemDefault()), userId, ip, method, ex.getMessage());
        }
    }

    /**
     * 현재 인증된 사용자 ID 가져오기
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "ANONYMOUS";
    }

    /**
     * 클라이언트 IP 주소 가져오기
     */
    private String getClientIP() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }

        return "UNKNOWN";
    }
}
