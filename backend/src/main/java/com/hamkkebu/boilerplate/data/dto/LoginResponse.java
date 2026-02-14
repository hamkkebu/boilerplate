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

    public static LoginResponse of(String username, String firstName, String lastName,
                                     String nickname, String email, TokenResponse token) {
        return LoginResponse.builder()
            .username(username)
            .firstName(firstName)
            .lastName(lastName)
            .nickname(nickname)
            .email(email)
            .token(token)
            .build();
    }
}
