package com.hamkkebu.authservice.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 중복 확인 응답 DTO
 *
 * <p>사용자 ID, 닉네임 등의 중복 여부를 확인하는 API의 응답입니다.</p>
 */
@Getter
@AllArgsConstructor
public class DuplicateCheckResponse {

    /**
     * 중복 여부
     * true: 이미 사용 중 (사용 불가)
     * false: 사용 가능
     */
    private final boolean exists;

    /**
     * 사용 가능 여부
     * true: 사용 가능
     * false: 이미 사용 중 (사용 불가)
     */
    private final boolean available;

    /**
     * 확인한 값
     */
    private final String value;

    /**
     * 중복 여부에 따른 응답 생성
     *
     * @param exists 중복 여부 (true: 중복 존재, false: 중복 없음)
     * @param value 확인한 값
     * @return DuplicateCheckResponse
     */
    public static DuplicateCheckResponse of(boolean exists, String value) {
        return new DuplicateCheckResponse(exists, !exists, value);
    }
}
