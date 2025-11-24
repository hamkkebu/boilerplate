package com.hamkkebu.boilerplate.common.config;

import com.hamkkebu.boilerplate.common.security.JwtAuthenticationEntryPoint;
import com.hamkkebu.boilerplate.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정
 *
 * <p>애플리케이션의 보안 정책을 설정합니다.</p>
 * <p>주요 설정:</p>
 * <ul>
 *   <li>CORS 설정</li>
 *   <li>JWT 인증 필터 적용</li>
 *   <li>세션 정책 (Stateless)</li>
 *   <li>API 엔드포인트별 권한 설정</li>
 *   <li>RBAC (Role-Based Access Control) 활성화</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final Environment environment;

    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private String[] allowedOrigins;

    /**
     * Security Filter Chain 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용 시 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 정책 설정 (Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 예외 처리 설정
                .exceptionHandling(exception -> exception
                        // 인증 실패 시 401 Unauthorized 반환
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // 요청별 권한 설정
                .authorizeHttpRequests(auth -> {
                    // 인증 없이 접근 가능한 엔드포인트
                    auth.requestMatchers(
                            // Auth API 중 인증 불필요한 엔드포인트만 허용
                            "/api/v1/auth/login",
                            "/api/v1/auth/refresh",
                            "/api/v1/auth/validate",
                            // 회원가입 및 중복 확인만 인증 불필요
                            "/api/v1/samples/check/**"
                    ).permitAll();

                    // 회원가입은 POST 메서드만 인증 불필요
                    auth.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/samples").permitAll();

                    // SECURITY: Actuator는 application.yml에서 제어
                    // dev: health,info,env,metrics
                    // prod: health,info만
                    // Health endpoint는 모니터링을 위해 공개, 나머지는 인증 필요
                    auth.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                    auth.requestMatchers("/actuator/**").authenticated();

                    // SECURITY: H2 Console - 개발 환경에서만 허용
                    if (isDevelopmentProfile()) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    // SECURITY: Swagger UI - 개발 환경에서만 허용
                    if (isDevelopmentProfile()) {
                        auth.requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll();
                    }

                    // SECURITY: Event Example API는 인증 필요 (개발/테스트용)
                    auth.requestMatchers("/api/v1/events/examples/**").authenticated();

                    // 그 외 모든 요청은 인증 필요 (Auth API의 /me, /logout 포함)
                    auth.anyRequest().authenticated();
                })

                // SECURITY: Security Headers 추가
                .headers(headers -> headers
                        // FIXED: Clickjacking 방어
                        // NOTE: H2 Console 사용 시 개발 환경에서만 frameOptions를 disable해야 함
                        // → application-dev.yml에서 spring.h2.console.enabled=true로 설정
                        // → 프로덕션(prod)에서는 deny (기본값)
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())  // SAMEORIGIN으로 변경 (H2 Console 호환)

                        // XSS 방어 (disable - 최신 브라우저는 Content-Security-Policy 사용)
                        .xssProtection(xss -> xss.disable())

                        // SECURITY: Content Security Policy (CSP)
                        // XSS, Clickjacking, Code Injection 방어
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:; " +
                                        "font-src 'self' data:; " +
                                        "connect-src 'self'; " +
                                        "frame-ancestors 'self'")
                        )

                        // FIXED: Content Type Sniffing 방지 (활성화)
                        .contentTypeOptions(contentType -> {})  // 기본값: nosniff 활성화

                        // HTTPS 강제 (HSTS)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000) // 1년
                        )
                        // Referrer Policy
                        .referrerPolicy(referrer -> referrer
                                .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                        // Permissions Policy
                        .permissionsPolicy(permissions -> permissions
                                .policy("geolocation=(), microphone=(), camera=()")
                        )
                )

                // JWT 인증 필터 추가
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * CORS 설정
     *
     * SECURITY 강화:
     * - allowedHeaders를 "*"에서 명시적 헤더 목록으로 제한
     * - XSS, CSRF 공격 벡터 최소화
     *
     * <p>환경 변수 cors.allowed-origins로 허용할 Origin을 설정합니다.</p>
     * <p>개발 환경: http://localhost:*, http://127.0.0.1:*</p>
     * <p>프로덕션 환경: 실제 도메인 (예: https://api.hamkkebu.com)</p>
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin 설정 (환경 변수로 주입)
        configuration.setAllowedOriginPatterns(List.of(allowedOrigins));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // SECURITY: 허용할 헤더 명시적 제한 ("*" 제거)
        // XSS, CSRF 공격 벡터 최소화
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Refresh-Token",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        // 인증 정보 허용 (JWT 토큰 사용)
        configuration.setAllowCredentials(true);

        // 노출할 헤더
        configuration.setExposedHeaders(List.of("Authorization", "Refresh-Token"));

        // SECURITY: Preflight 요청 캐시 시간 설정 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 비밀번호 인코더 (BCrypt)
     *
     * SECURITY: BCrypt Rounds 증가 (10 → 12)
     * - Rounds: 2^12 = 4096 iterations
     * - GPU 브루트포스 공격 방어 강화
     * - 성능: ~400ms (10 rounds: ~100ms)
     * - 권장: Production에서는 12-14 rounds
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // SECURITY: BCrypt strength 증가
        // - Default: 10 rounds
        // - Recommended: 12-14 rounds
        return new BCryptPasswordEncoder(12);
    }

    /**
     * 개발 환경 여부 확인
     *
     * @return dev 프로파일 활성화 시 true
     */
    private boolean isDevelopmentProfile() {
        return environment != null &&
                Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }
}
