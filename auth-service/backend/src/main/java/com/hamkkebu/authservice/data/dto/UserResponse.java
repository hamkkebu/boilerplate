package com.hamkkebu.authservice.data.dto;

import com.hamkkebu.boilerplate.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String country;
    private String city;
    private String state;
    private String streetAddress;
    private String postalCode;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime lastLoginAt;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
