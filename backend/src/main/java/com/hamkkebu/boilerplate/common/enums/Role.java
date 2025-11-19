package com.hamkkebu.boilerplate.common.enums;

/**
 * 사용자 권한 Enum
 *
 * <p>RBAC (Role-Based Access Control) 구현을 위한 사용자 역할 정의</p>
 *
 * <ul>
 *   <li>USER: 일반 사용자 (기본 권한)</li>
 *   <li>ADMIN: 관리자 (모든 사용자 데이터 조회/수정 가능)</li>
 *   <li>DEVELOPER: 개발자 (개발용 API 접근 가능)</li>
 * </ul>
 *
 * <p>권한 계층:</p>
 * <ul>
 *   <li>DEVELOPER > ADMIN > USER</li>
 *   <li>상위 권한은 하위 권한의 모든 권한을 포함</li>
 * </ul>
 */
public enum Role {
    /**
     * 일반 사용자
     * - 본인의 데이터만 조회/수정/삭제 가능
     * - 기본 권한
     */
    USER("ROLE_USER"),

    /**
     * 관리자
     * - 모든 사용자 데이터 조회 가능
     * - 사용자 관리 기능 사용 가능
     * - 통계 및 분석 데이터 조회 가능
     */
    ADMIN("ROLE_ADMIN"),

    /**
     * 개발자
     * - 개발용 API 접근 가능 (Event Example API 등)
     * - 시스템 설정 조회 가능
     * - ADMIN 권한 포함
     */
    DEVELOPER("ROLE_DEVELOPER");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    /**
     * Spring Security의 GrantedAuthority 형식으로 변환
     *
     * @return "ROLE_" 접두사가 포함된 권한 문자열
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * 문자열로부터 Role 생성
     *
     * @param value "USER", "ADMIN", "DEVELOPER" 또는 "ROLE_USER", "ROLE_ADMIN", "ROLE_DEVELOPER"
     * @return Role enum
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static Role fromString(String value) {
        if (value == null) {
            return USER; // 기본값
        }

        // "ROLE_" 접두사 제거
        String normalizedValue = value.startsWith("ROLE_") ? value.substring(5) : value;

        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(normalizedValue)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Invalid role: " + value);
    }
}
