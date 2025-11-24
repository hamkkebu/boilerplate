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

/**
 * 보안 감사 로그 시스템 (AOP 기반)
 *
 * <p>중요한 보안 이벤트를 자동으로 로깅합니다.</p>
 *
 * <p>로깅 대상 이벤트:</p>
 * <ul>
 *   <li>로그인 성공/실패</li>
 *   <li>로그아웃</li>
 *   <li>회원가입</li>
 *   <li>회원 탈퇴</li>
 *   <li>토큰 갱신</li>
 *   <li>권한 거부 (ACCESS_DENIED)</li>
 * </ul>
 *
 * <p>로그 포맷:</p>
 * <pre>
 * [SECURITY_AUDIT] timestamp | event | userId | ip | status | details
 * </pre>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAuditLogger {

    /**
     * 로그인 성공 로깅
     */
    @AfterReturning(
            pointcut = "execution(* com.hamkkebu.boilerplate.service.AuthService.login(..))",
            returning = "result"
    )
    public void logLoginSuccess(JoinPoint joinPoint, Object result) {
        String userId = extractUserIdFromArgs(joinPoint);
        String ip = getClientIP();

        log.info("[SECURITY_AUDIT] {} | EVENT=LOGIN_SUCCESS | USER={} | IP={} | STATUS=SUCCESS",
                LocalDateTime.now(), userId, ip);
    }

    /**
     * 로그인 실패 로깅
     */
    @AfterThrowing(
            pointcut = "execution(* com.hamkkebu.boilerplate.service.AuthService.login(..))",
            throwing = "ex"
    )
    public void logLoginFailure(JoinPoint joinPoint, Exception ex) {
        String userId = extractUserIdFromArgs(joinPoint);
        String ip = getClientIP();

        log.warn("[SECURITY_AUDIT] {} | EVENT=LOGIN_FAILURE | USER={} | IP={} | STATUS=FAILED | REASON={}",
                LocalDateTime.now(), userId, ip, ex.getMessage());
    }

    /**
     * 로그아웃 로깅
     */
    @AfterReturning(
            pointcut = "execution(* com.hamkkebu.boilerplate.service.AuthService.logout(..))"
    )
    public void logLogout(JoinPoint joinPoint) {
        String userId = getCurrentUserId();
        String ip = getClientIP();

        log.info("[SECURITY_AUDIT] {} | EVENT=LOGOUT | USER={} | IP={} | STATUS=SUCCESS",
                LocalDateTime.now(), userId, ip);
    }

    /**
     * 회원가입 로깅
     */
    @AfterReturning(
            pointcut = "execution(* com.hamkkebu.boilerplate.service.SampleService.createSample(..))",
            returning = "result"
    )
    public void logUserRegistration(JoinPoint joinPoint, Object result) {
        String userId = extractUserIdFromArgs(joinPoint);
        String ip = getClientIP();

        log.info("[SECURITY_AUDIT] {} | EVENT=USER_REGISTRATION | USER={} | IP={} | STATUS=SUCCESS",
                LocalDateTime.now(), userId, ip);
    }

    /**
     * 회원 탈퇴 로깅
     */
    @AfterReturning(
            pointcut = "execution(* com.hamkkebu.boilerplate.service.SampleService.deleteSample(..))"
    )
    public void logUserDeletion(JoinPoint joinPoint) {
        String userId = extractUserIdFromArgs(joinPoint);
        String ip = getClientIP();

        log.info("[SECURITY_AUDIT] {} | EVENT=USER_DELETION | USER={} | IP={} | STATUS=SUCCESS",
                LocalDateTime.now(), userId, ip);
    }

    /**
     * 토큰 갱신 로깅
     */
    @AfterReturning(
            pointcut = "execution(* com.hamkkebu.boilerplate.service.AuthService.refresh(..))"
    )
    public void logTokenRefresh(JoinPoint joinPoint) {
        String userId = getCurrentUserId();
        String ip = getClientIP();

        log.info("[SECURITY_AUDIT] {} | EVENT=TOKEN_REFRESH | USER={} | IP={} | STATUS=SUCCESS",
                LocalDateTime.now(), userId, ip);
    }

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
                    LocalDateTime.now(), key, ip);
        }
    }

    /**
     * 권한 거부 로깅 (IDOR 등)
     */
    @AfterThrowing(
            pointcut = "execution(* com.hamkkebu.boilerplate.controller..*(..))",
            throwing = "ex"
    )
    public void logAccessDenied(JoinPoint joinPoint, Exception ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("권한")) {
            String userId = getCurrentUserId();
            String ip = getClientIP();
            String method = joinPoint.getSignature().toShortString();

            log.warn("[SECURITY_AUDIT] {} | EVENT=ACCESS_DENIED | USER={} | IP={} | METHOD={} | STATUS=DENIED | REASON={}",
                    LocalDateTime.now(), userId, ip, method, ex.getMessage());
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
     * 메서드 인자에서 사용자 ID 추출
     */
    private String extractUserIdFromArgs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] != null) {
            // LoginRequest 또는 RequestSample에서 ID 추출
            String argString = args[0].toString();
            if (argString.contains("sampleId=")) {
                int startIdx = argString.indexOf("sampleId=") + 9;
                int endIdx = argString.indexOf(",", startIdx);
                if (endIdx == -1) endIdx = argString.indexOf(")", startIdx);
                return argString.substring(startIdx, endIdx).trim();
            }
        }
        return "UNKNOWN";
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
