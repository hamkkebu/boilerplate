package com.hamkkebu.boilerplate.common.config;

import com.hamkkebu.boilerplate.common.security.JwtAuthenticationEntryPoint;
import com.hamkkebu.boilerplate.common.security.JwtAuthenticationFilter;
import com.hamkkebu.boilerplate.common.security.KeycloakJwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
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
 * <p>Keycloak OAuth2 또는 자체 JWT 인증을 지원합니다.</p>
 * <p>keycloak.enabled=true 설정 시 Keycloak OAuth2 Resource Server로 동작합니다.</p>
 *
 * <p>주요 설정:</p>
 * <ul>
 *   <li>CORS 설정</li>
 *   <li>Keycloak OAuth2 Resource Server (SSO)</li>
 *   <li>세션 정책 (Stateless)</li>
 *   <li>API 엔드포인트별 권한 설정</li>
 *   <li>RBAC (Role-Based Access Control) 활성화</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;
    private final Environment environment;

    @Value("${cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private String[] allowedOrigins;

    @Value("${keycloak.enabled:false}")
    private boolean keycloakEnabled;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String jwtIssuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

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
                            // 회원가입 및 중복 확인
                            "/api/v1/users/check/**",
                            "/api/v1/samples/check/**"
                    ).permitAll();

                    // 회원가입은 POST 메서드만 인증 불필요
                    auth.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/users", "/api/v1/samples").permitAll();

                    // Health endpoint는 모니터링을 위해 공개
                    auth.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                    auth.requestMatchers("/actuator/**").authenticated();

                    // H2 Console - 개발 환경에서만 허용
                    if (isDevelopmentProfile()) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    // Swagger UI - 개발 환경에서만 허용
                    if (isDevelopmentProfile()) {
                        auth.requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll();
                    }

                    // Event Example API는 인증 필요
                    auth.requestMatchers("/api/v1/events/examples/**").authenticated();

                    // 그 외 모든 요청은 인증 필요
                    auth.anyRequest().authenticated();
                })

                // Security Headers 추가
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                        .xssProtection(xss -> xss.disable())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:; " +
                                        "font-src 'self' data:; " +
                                        "connect-src 'self'; " +
                                        "frame-ancestors 'self'")
                        )
                        .contentTypeOptions(contentType -> {})
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                        .referrerPolicy(referrer -> referrer
                                .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                        .permissionsPolicy(permissions -> permissions
                                .policy("geolocation=(), microphone=(), camera=()")
                        )
                );

        // Keycloak OAuth2 Resource Server 또는 자체 JWT 필터 적용
        if (keycloakEnabled) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt
                            .jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)
                    )
            );
        } else {
            // 기존 JWT 인증 필터 사용
            http.addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );
        }

        return http.build();
    }

    /**
     * JWT Decoder (Keycloak 사용 시)
     */
    @Bean
    @ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true")
    public JwtDecoder jwtDecoder() {
        if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }
        // issuer-uri를 사용하는 경우 Spring Boot가 자동 설정
        return null;
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(allowedOrigins));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Refresh-Token",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(List.of("Authorization", "Refresh-Token"));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 비밀번호 인코더 (BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * 개발 환경 여부 확인
     */
    private boolean isDevelopmentProfile() {
        return environment != null &&
                Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }
}
