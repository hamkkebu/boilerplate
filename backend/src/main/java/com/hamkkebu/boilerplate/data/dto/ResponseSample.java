package com.hamkkebu.boilerplate.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Sample 응답 DTO
 *
 * <p>Sample 조회 시 클라이언트에게 반환되는 데이터 형식</p>
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSample {

    /**
     * Sample 번호 (PK)
     */
    private Long sampleNum;

    /**
     * Sample ID (Unique)
     */
    private String sampleId;

    /**
     * First Name
     */
    private String sampleFname;

    /**
     * Last Name
     */
    private String sampleLname;
}
