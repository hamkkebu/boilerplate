package com.hamkkebu.boilerplate.data.dto;

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
public class RequestSample {

    @NotBlank(message = "sampleId는 필수입니다")
    @Size(min = 3, max = 20, message = "sampleId는 3자 이상 20자 이하여야 합니다")
    private String sampleId;

    @NotBlank(message = "firstName은 필수입니다")
    @Size(min = 1, max = 50, message = "firstName은 1자 이상 50자 이하여야 합니다")
    private String sampleFname;

    @NotBlank(message = "lastName은 필수입니다")
    @Size(min = 1, max = 50, message = "lastName은 1자 이상 50자 이하여야 합니다")
    private String sampleLname;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String sampleEmail;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(
        regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
        message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)"
    )
    private String samplePhone;

    // 선택 필드들 (validation 없음)
    private String sampleNickname;
    private String sampleCountry;
    private String sampleCity;
    private String sampleState;
    private String sampleStreet1;
    private String sampleStreet2;
    private String sampleZip;
}
