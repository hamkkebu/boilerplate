package com.hamkkebu.boilerplate.common.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * HTTP to HTTPS 리다이렉트 설정
 *
 * <p>HTTP(8080)로 들어오는 요청을 HTTPS(8443)로 자동 리다이렉트합니다.</p>
 *
 * <p>동작 방식:</p>
 * <ul>
 *   <li>HTTP 커넥터(8080)를 추가로 생성</li>
 *   <li>모든 HTTP 요청을 HTTPS로 리다이렉트</li>
 *   <li>보안 연결 강제</li>
 * </ul>
 *
 * <p>프로파일:</p>
 * <ul>
 *   <li>dev: 개발 환경에서 활성화 (자체 서명 인증서)</li>
 *   <li>prod: 프로덕션 환경에서 활성화 (Let's Encrypt 인증서)</li>
 * </ul>
 *
 * <p>주의: SSL이 활성화된 경우에만 이 설정이 필요합니다.</p>
 */
@Configuration
@Profile({"prod"}) // SSL이 활성화된 프로파일에서만 활성화 (dev는 제외 - 로컬에서는 HTTPS 미사용)
public class HttpToHttpsRedirectConfig {

    @Value("${server.http.port:8080}")
    private int httpPort;

    @Value("${server.port:8443}")
    private int httpsPort;

    /**
     * Tomcat 서버 팩토리 설정
     *
     * <p>HTTP 커넥터를 추가하고 HTTPS로 리다이렉트하도록 설정</p>
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                // HTTPS로 리다이렉트하는 보안 제약 조건 설정
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");

                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");  // 모든 경로
                securityConstraint.addCollection(collection);

                context.addConstraint(securityConstraint);
            }
        };

        // HTTP 커넥터 추가
        tomcat.addAdditionalTomcatConnectors(createHttpConnector());

        return tomcat;
    }

    /**
     * HTTP 커넥터 생성
     *
     * <p>HTTP(8080) 포트로 들어오는 요청을 받기 위한 커넥터</p>
     *
     * @return HTTP 커넥터
     */
    private Connector createHttpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        connector.setRedirectPort(httpsPort);  // HTTPS 포트로 리다이렉트

        return connector;
    }
}
