package com.hamkkebu.boilerplate.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String username;
    private String firstName;
    private String lastName;
    private String nickname;
    private String email;
    private TokenResponse token;

    public static LoginResponse of(SampleResponse sample, TokenResponse token) {
        return LoginResponse.builder()
            .username(sample.getUsername())
            .firstName(sample.getFirstName())
            .lastName(sample.getLastName())
            .nickname(sample.getNickname())
            .email(sample.getEmail())
            .token(token)
            .build();
    }
}
