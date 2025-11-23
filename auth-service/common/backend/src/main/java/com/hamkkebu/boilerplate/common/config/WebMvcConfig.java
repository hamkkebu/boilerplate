package com.hamkkebu.boilerplate.common.config;

import com.hamkkebu.boilerplate.common.security.RateLimitingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 *
 * <p>Rate Limiting Interceptor 등록</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitingInterceptor rateLimitingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/**")  // 모든 API에 적용
                .excludePathPatterns(
                        "/actuator/**",      // Actuator 제외
                        "/h2-console/**",    // H2 Console 제외
                        "/swagger-ui/**",    // Swagger UI 제외
                        "/v3/api-docs/**"    // API Docs 제외
                );
    }
}
