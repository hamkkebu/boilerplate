package com.hamkkebu.boilerplate.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Keycloak JWT를 Spring Security Authentication으로 변환
 *
 * <p>Keycloak JWT 토큰의 realm_access.roles를 Spring Security authorities로 매핑합니다.</p>
 */
@Slf4j
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String principalName = extractPrincipalName(jwt);

        log.debug("Converted JWT for user: {}, authorities: {}", principalName, authorities);

        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    /**
     * JWT에서 권한(roles) 추출
     *
     * <p>Keycloak JWT 구조:</p>
     * <pre>
     * {
     *   "realm_access": {
     *     "roles": ["USER", "ADMIN"]
     *   },
     *   "resource_access": {
     *     "hamkkebu-backend": {
     *       "roles": ["view", "edit"]
     *     }
     *   }
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. Realm roles 추출
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (realmAccess != null && realmAccess.containsKey(ROLES_CLAIM)) {
            List<String> roles = (List<String>) realmAccess.get(ROLES_CLAIM);
            authorities.addAll(
                    roles.stream()
                            .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                            .collect(Collectors.toList())
            );

            // ROLE_ prefix 없는 버전도 추가 (기존 코드 호환성)
            authorities.addAll(
                    roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList())
            );
        }

        // 2. Resource (client) roles 추출 (선택적)
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.forEach((clientId, access) -> {
                if (access instanceof Map) {
                    Map<String, Object> clientAccess = (Map<String, Object>) access;
                    if (clientAccess.containsKey(ROLES_CLAIM)) {
                        List<String> clientRoles = (List<String>) clientAccess.get(ROLES_CLAIM);
                        authorities.addAll(
                                clientRoles.stream()
                                        .map(role -> new SimpleGrantedAuthority(clientId + "_" + role))
                                        .collect(Collectors.toList())
                        );
                    }
                }
            });
        }

        return authorities;
    }

    /**
     * JWT에서 사용자 식별자 추출
     *
     * <p>우선순위: preferred_username > sub</p>
     */
    private String extractPrincipalName(Jwt jwt) {
        // preferred_username이 있으면 사용 (Keycloak 기본)
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        // 없으면 subject (user id) 사용
        return jwt.getSubject();
    }
}
