package com.hamkkebu.boilerplate.common.enums;

/**
 * 가계부 공유 권한 Enum
 *
 * <p>공유 가계부에 대한 접근 권한 수준을 정의합니다.</p>
 *
 * <ul>
 *   <li>READ_ONLY: 읽기 전용 (현재 기본값)</li>
 *   <li>READ_WRITE: 읽기/쓰기 (향후 확장)</li>
 *   <li>ADMIN: 관리자 권한 (향후 확장)</li>
 * </ul>
 *
 * <p>권한 계층: ADMIN > READ_WRITE > READ_ONLY</p>
 */
public enum SharePermission {

    /**
     * 읽기 전용
     * - 가계부 조회만 가능
     * - 거래 내역 조회 가능
     * - 통계 조회 가능
     * - 수정/삭제 불가
     */
    READ_ONLY,

    /**
     * 읽기/쓰기 (향후 확장)
     * - READ_ONLY 권한 포함
     * - 거래 내역 생성/수정/삭제 가능
     * - 카테고리 수정 불가
     */
    READ_WRITE,

    /**
     * 관리자 (향후 확장)
     * - READ_WRITE 권한 포함
     * - 카테고리 관리 가능
     * - 다른 사용자에게 공유 가능
     * - 가계부 설정 변경 불가 (소유자만 가능)
     */
    ADMIN
}
