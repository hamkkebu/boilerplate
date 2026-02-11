package com.hamkkebu.boilerplate.common.enums;

/**
 * 가계부 공유 상태 Enum
 *
 * <p>가계부 공유 요청의 생명주기를 정의합니다.</p>
 *
 * <ul>
 *   <li>PENDING: 공유 요청 대기 중 (초기 상태)</li>
 *   <li>ACCEPTED: 공유 요청 수락됨</li>
 *   <li>REJECTED: 공유 요청 거절됨</li>
 * </ul>
 *
 * <p>상태 전이:</p>
 * <pre>
 * PENDING → ACCEPTED (수신자가 수락)
 * PENDING → REJECTED (수신자가 거절)
 * </pre>
 */
public enum ShareStatus {

    /**
     * 공유 요청 대기 중
     * - 소유자가 공유 요청을 보낸 초기 상태
     * - 수신자가 아직 응답하지 않음
     */
    PENDING,

    /**
     * 공유 요청 수락됨
     * - 수신자가 공유 요청을 수락
     * - 공유 가계부 조회 가능
     */
    ACCEPTED,

    /**
     * 공유 요청 거절됨
     * - 수신자가 공유 요청을 거절
     * - 공유 가계부 접근 불가
     */
    REJECTED
}
