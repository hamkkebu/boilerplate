package com.hamkkebu.boilerplate.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 거래 이벤트 발행 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PublishTransactionEventRequest {

    @NotBlank(message = "transactionId는 필수입니다")
    @Size(min = 3, max = 50, message = "transactionId는 3자 이상 50자 이하여야 합니다")
    private String transactionId;

    @NotBlank(message = "userId는 필수입니다")
    @Size(min = 3, max = 50, message = "userId는 3자 이상 50자 이하여야 합니다")
    private String userId;

    @NotBlank(message = "ledgerId는 필수입니다")
    @Size(min = 3, max = 50, message = "ledgerId는 3자 이상 50자 이하여야 합니다")
    private String ledgerId;
}
