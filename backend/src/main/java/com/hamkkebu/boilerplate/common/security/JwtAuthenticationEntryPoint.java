package com.hamkkebu.boilerplate.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증 실패 처리 핸들러
 *
 * <p>인증되지 않은 요청에 대해 401 Unauthorized 응답을 반환합니다.</p>
 *
 * FIX: ObjectMapper를 Spring Bean으로 주입받아 LocalDateTime 직렬화 지원
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // FIXED: Spring Boot의 자동 설정된 ObjectMapper 주입 (JavaTimeModule 포함)
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        log.warn("Unauthorized request to {}: {}", request.getRequestURI(), authException.getMessage());

        // 401 Unauthorized 응답 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // API 응답 형식으로 에러 메시지 작성
        ApiResponse<Void> apiResponse = ApiResponse.error(
                ErrorCode.AUTHENTICATION_FAILED.getCode(),
                "인증이 필요합니다. 로그인 후 다시 시도해주세요."
        );

        // JSON 응답 작성
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
