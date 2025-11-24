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
public class SampleResponse {

    /**
     * Sample 번호 (PK)
     */
    private Long id;

    /**
     * 사용자 ID (Unique)
     */
    private String username;

    /**
     * 이름
     */
    private String firstName;

    /**
     * 성
     */
    private String lastName;

    /**
     * 닉네임
     */
    private String nickname;

    /**
     * 이메일
     */
    private String email;

    /**
     * 전화번호
     */
    private String phone;

    /**
     * 국가
     */
    private String country;

    /**
     * 도시
     */
    private String city;

    /**
     * 주/도
     */
    private String state;

    /**
     * 주소 1
     */
    private String street1;

    /**
     * 주소 2
     */
    private String street2;

    /**
     * 우편번호
     */
    private String zip;
}
