package com.hamkkebu.authservice.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 등록/수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "사용자 이름은 필수입니다")
    @Size(min = 3, max = 50, message = "사용자 이름은 3-50자 사이여야 합니다")
    private String username;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다")
    private String password;

    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    private String firstName;

    @Size(max = 50, message = "성은 50자 이하여야 합니다")
    private String lastName;

    // Frontend에서는 "phone"으로 전송하므로 매핑
    @JsonProperty("phone")
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    private String phoneNumber;

    @Size(max = 50, message = "국가는 50자 이하여야 합니다")
    private String country;

    @Size(max = 50, message = "도시는 50자 이하여야 합니다")
    private String city;

    @Size(max = 50, message = "주/도는 50자 이하여야 합니다")
    private String state;

    // Frontend에서는 "street1"으로 전송하므로 매핑
    @JsonProperty("street1")
    @Size(max = 200, message = "주소는 200자 이하여야 합니다")
    private String streetAddress;

    // Frontend에서는 "zip"으로 전송하므로 매핑
    @JsonProperty("zip")
    @Size(max = 20, message = "우편번호는 20자 이하여야 합니다")
    private String postalCode;
}
