package com.hamkkebu.authservice.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 삭제(탈퇴) 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserRequest {

    /**
     * 비밀번호 (탈퇴 확인용)
     */
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
