package com.hamkkebu.boilerplate.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 이벤트 발행 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PublishUserEventRequest {

    @NotBlank(message = "userId는 필수입니다")
    @Size(min = 3, max = 50, message = "userId는 3자 이상 50자 이하여야 합니다")
    private String userId;

    @Size(max = 500, message = "metadata는 500자 이하여야 합니다")
    private String metadata;
}
