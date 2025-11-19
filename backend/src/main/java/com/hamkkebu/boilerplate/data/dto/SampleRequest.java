package com.hamkkebu.boilerplate.data.dto;

import com.hamkkebu.boilerplate.common.constant.CommonConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Sample 생성/수정 요청 DTO
 *
 * <p>Validation 적용:</p>
 * <ul>
 *   <li>@NotBlank: 필수 값, null/빈 문자열/공백 불가</li>
 *   <li>@Size: 문자열 길이 제한</li>
 *   <li>@Email: 이메일 형식 검증</li>
 *   <li>@Pattern: 정규식 패턴 검증</li>
 * </ul>
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SampleRequest {

    @NotBlank(message = "사용자 ID는 필수입니다")
    @Size(min = 3, max = 20, message = "사용자 ID는 3자 이상 20자 이하여야 합니다")
    private String username;

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 1, max = 50, message = "이름은 1자 이상 50자 이하여야 합니다")
    private String firstName;

    @NotBlank(message = "성은 필수입니다")
    @Size(min = 1, max = 50, message = "성은 1자 이상 50자 이하여야 합니다")
    private String lastName;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(
        regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
        message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)"
    )
    private String phone;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(
        min = CommonConstants.PASSWORD_MIN_LENGTH,
        max = CommonConstants.PASSWORD_MAX_LENGTH,
        message = "비밀번호는 8자 이상 100자 이하여야 합니다"
    )
    @Pattern(
        regexp = CommonConstants.PASSWORD_REGEX,
        message = "비밀번호는 8자 이상이며, 영문자, 숫자, 특수문자를 모두 포함해야 합니다"
    )
    private String password;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다")
    private String nickname;

    // 선택 필드들 (validation 없음)
    private String country;
    private String city;
    private String state;
    private String street1;
    private String street2;
    private String zip;
}
