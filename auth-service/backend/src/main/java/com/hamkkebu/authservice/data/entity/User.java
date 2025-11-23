package com.hamkkebu.authservice.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hamkkebu.boilerplate.common.entity.BaseEntity;
import com.hamkkebu.boilerplate.common.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * User 엔티티
 *
 * <p>사용자(회원) 정보를 저장하는 엔티티입니다.</p>
 * <p>BaseEntity를 상속받아 생성/수정 일시 및 Soft Delete 기능을 지원합니다.</p>
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "street_address", length = 200)
    private String streetAddress;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 사용자 권한
     * RBAC (Role-Based Access Control) 구현
     *
     * <p>기본값: USER</p>
     * <p>권한 종류: USER, ADMIN, DEVELOPER</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    /**
     * 비밀번호 업데이트
     *
     * <p>암호화된 비밀번호로 업데이트합니다.</p>
     *
     * @param encodedPassword 암호화된 비밀번호
     */
    public void updatePassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 사용자 권한 변경
     *
     * @param role 변경할 권한
     */
    public void updateRole(Role role) {
        this.role = role;
    }

    /**
     * 사용자 활성화 상태 변경
     *
     * @param isActive 활성화 여부
     */
    public void updateActiveStatus(Boolean isActive) {
        this.isActive = isActive;
    }
}
