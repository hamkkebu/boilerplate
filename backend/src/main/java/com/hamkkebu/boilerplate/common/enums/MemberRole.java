package com.hamkkebu.boilerplate.common.enums;

/**
 * 가계부 멤버 역할 열거형
 *
 * <p>가계부에 속한 멤버들의 역할과 권한을 정의합니다.</p>
 */
public enum MemberRole {
    OWNER,    // 가계부 소유자 - 전체 권한 + 삭제/양도
    ADMIN,    // 관리자 - 카테고리/카드 관리, 멤버 초대
    MEMBER,   // 일반 멤버 - 거래 기록
    VIEWER;   // 조회 전용

    /**
     * 이 역할이 쓰기 권한을 가지고 있는지 확인합니다.
     *
     * @return 쓰기 권한이 있으면 true
     */
    public boolean hasWriteAccess() {
        return this == OWNER || this == ADMIN || this == MEMBER;
    }

    /**
     * 이 역할이 관리자 권한을 가지고 있는지 확인합니다.
     *
     * @return 관리자 권한이 있으면 true
     */
    public boolean hasAdminAccess() {
        return this == OWNER || this == ADMIN;
    }

    /**
     * 이 역할이 소유자인지 확인합니다.
     *
     * @return 소유자이면 true
     */
    public boolean isOwner() {
        return this == OWNER;
    }
}
