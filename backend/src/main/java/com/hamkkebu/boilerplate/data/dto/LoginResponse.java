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

    private String sampleId;
    private String sampleFname;
    private String sampleLname;
    private String sampleNickname;
    private String sampleEmail;
    private TokenResponse token;

    public static LoginResponse of(ResponseSample sample, TokenResponse token) {
        return LoginResponse.builder()
            .sampleId(sample.getSampleId())
            .sampleFname(sample.getSampleFname())
            .sampleLname(sample.getSampleLname())
            .sampleNickname(sample.getSampleNickname())
            .sampleEmail(sample.getSampleEmail())
            .token(token)
            .build();
    }
}
