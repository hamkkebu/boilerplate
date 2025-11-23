package com.hamkkebu.boilerplate.common.test;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

/**
 * OpenAPI 문서 생성 설정
 *
 * <p>REST Docs로 생성된 스니펫을 OpenAPI 3.0 스펙으로 변환합니다.</p>
 *
 * <p>생성 위치:</p>
 * <ul>
 *   <li>테스트 실행: build/api-spec/openapi3.yaml</li>
 *   <li>bootJar 실행: build/resources/main/static/docs/openapi3.yaml</li>
 * </ul>
 */
@TestConfiguration
public class OpenApiConfig {

    /**
     * REST Docs 전처리 설정
     * <p>요청/응답을 보기 좋게 포맷팅합니다.</p>
     */
    @Bean
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer() {
        return configurer -> configurer.operationPreprocessors()
            .withRequestDefaults(prettyPrint())
            .withResponseDefaults(prettyPrint());
    }
}
