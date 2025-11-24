package com.hamkkebu.boilerplate.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 *
 * <p>접속 주소:</p>
 * <ul>
 *   <li>Swagger UI: http://localhost:8080/swagger-ui.html</li>
 *   <li>OpenAPI JSON: http://localhost:8080/v3/api-docs</li>
 *   <li>OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml</li>
 * </ul>
 *
 * <p>JWT 인증:</p>
 * <ul>
 *   <li>Swagger UI 우측 상단 "Authorize" 버튼 클릭</li>
 *   <li>Bearer Token 입력 (예: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...)</li>
 *   <li>자동으로 Authorization 헤더에 "Bearer {token}" 형식으로 추가됨</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    @Value("${server.description:Local Development Server}")
    private String serverDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Hamkkebu Boilerplate API")
                .description("Hamkkebu Boilerplate REST API 문서")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Hamkkebu Team")
                    .email("support@hamkkebu.com")
                )
            )
            .servers(List.of(
                new Server()
                    .url(serverUrl)
                    .description(serverDescription)
            ))
            // JWT Bearer Token 인증 설정
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT 토큰을 입력하세요 (Bearer 접두사 없이)")
                )
            )
            // 모든 API에 보안 요구사항 적용 (개별 API에서 재정의 가능)
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
