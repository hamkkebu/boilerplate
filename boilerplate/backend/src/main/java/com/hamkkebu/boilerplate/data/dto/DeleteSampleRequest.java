package com.hamkkebu.boilerplate.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Sample 삭제 요청 DTO
 *
 * <p>회원 탈퇴 시 비밀번호 검증을 위한 DTO입니다.</p>
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteSampleRequest {

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
