package com.hamkkebu.boilerplate.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
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
 */
@Configuration
public class OpenApiConfig {

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
                    .url("http://localhost:8080")
                    .description("Local Development Server"),
                new Server()
                    .url("https://api.hamkkebu.com")
                    .description("Production Server")
            ));
    }
}
