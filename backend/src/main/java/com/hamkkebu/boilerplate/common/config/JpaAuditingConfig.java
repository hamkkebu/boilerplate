package com.hamkkebu.boilerplate.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing 설정
 *
 * <p>엔티티의 생성/수정 정보를 자동으로 관리하는 JPA Auditing 기능을 활성화합니다.</p>
 *
 * <p>제공하는 기능:</p>
 * <ul>
 *   <li>@CreatedDate, @LastModifiedDate: 생성/수정 일시 자동 설정</li>
 *   <li>@CreatedBy, @LastModifiedBy: 생성/수정자 자동 설정</li>
 * </ul>
 *
 * <p>BaseEntity를 상속받은 모든 엔티티에 자동으로 적용됩니다.</p>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * 현재 사용자 정보를 제공하는 AuditorAware 빈
     *
     * <p>@CreatedBy, @LastModifiedBy 어노테이션이 붙은 필드에 자동으로 주입됩니다.</p>
     *
     * <p>사용자 정보 조회 우선순위:</p>
     * <ol>
     *   <li>Spring Security Context에서 인증된 사용자 ID 조회</li>
     *   <li>인증 정보가 없으면 "system" 반환 (시스템 작업)</li>
     * </ol>
     *
     * @return 현재 사용자 ID를 제공하는 AuditorAware 구현체
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    /**
     * AuditorAware 구현 클래스
     *
     * <p>Spring Security Context에서 현재 로그인한 사용자 정보를 가져옵니다.</p>
     */
    static class AuditorAwareImpl implements AuditorAware<String> {

        /**
         * 현재 사용자 ID 반환
         *
         * <p>반환 로직:</p>
         * <ul>
         *   <li>인증된 사용자가 있으면 사용자 ID 반환</li>
         *   <li>인증 정보가 없으면 "system" 반환</li>
         * </ul>
         *
         * @return 현재 사용자 ID (Optional)
         */
        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 인증 정보가 없거나, 인증되지 않았거나, 익명 사용자인 경우
            if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
                return Optional.of("system");
            }

            // 인증된 사용자 ID 반환
            // JwtAuthenticationFilter에서 userId를 Principal로 설정함
            String userId = authentication.getName();

            return Optional.ofNullable(userId);
        }
    }
}
