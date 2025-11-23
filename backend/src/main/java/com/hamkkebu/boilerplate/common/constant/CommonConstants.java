package com.hamkkebu.boilerplate.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 공통 상수 클래스
 *
 * <p>애플리케이션 전반에서 사용되는 공통 상수를 정의합니다.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonConstants {

    // ==================== API 관련 ====================

    /**
     * API 버전
     */
    public static final String API_VERSION = "/api/v1";

    /**
     * API 기본 응답 메시지
     */
    public static final String API_SUCCESS_MESSAGE = "Success";

    // ==================== 페이징 관련 ====================

    /**
     * 기본 페이지 번호
     */
    public static final int DEFAULT_PAGE = 0;

    /**
     * 기본 페이지 크기
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 최대 페이지 크기
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 기본 정렬 방향
     */
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // ==================== 날짜/시간 관련 ====================

    /**
     * 기본 날짜 포맷
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 기본 날짜시간 포맷
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * ISO 8601 날짜시간 포맷
     */
    public static final String DATETIME_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * 기본 타임존
     */
    public static final String DEFAULT_TIMEZONE = "Asia/Seoul";

    // ==================== 인증/인가 관련 ====================

    /**
     * JWT 토큰 헤더 이름
     */
    public static final String AUTH_HEADER = "Authorization";

    /**
     * JWT 토큰 접두사
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * JWT 토큰 타입
     */
    public static final String TOKEN_TYPE = "Bearer";

    /**
     * 리프레시 토큰 헤더 이름
     */
    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";

    // ==================== 사용자 관련 ====================

    /**
     * 기본 사용자 역할
     */
    public static final String DEFAULT_USER_ROLE = "ROLE_USER";

    /**
     * 관리자 역할
     */
    public static final String ADMIN_ROLE = "ROLE_ADMIN";

    /**
     * 익명 사용자
     */
    public static final String ANONYMOUS_USER = "anonymousUser";

    // ==================== Kafka 관련 ====================

    /**
     * 사용자 이벤트 토픽
     */
    public static final String TOPIC_USER_EVENTS = "user.events";

    /**
     * 가계부 이벤트 토픽
     */
    public static final String TOPIC_LEDGER_EVENTS = "ledger.events";

    /**
     * 거래 이벤트 토픽
     */
    public static final String TOPIC_TRANSACTION_EVENTS = "transaction.events";

    /**
     * 알림 이벤트 토픽
     */
    public static final String TOPIC_NOTIFICATION_EVENTS = "notification.events";

    // ==================== 파일 관련 ====================

    /**
     * 최대 파일 크기 (10MB)
     */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 허용된 파일 확장자
     */
    public static final String[] ALLOWED_FILE_EXTENSIONS = {
        "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx"
    };

    /**
     * 허용된 이미지 확장자
     */
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {
        "jpg", "jpeg", "png", "gif"
    };

    // ==================== 검증 관련 ====================

    /**
     * 이메일 정규식
     */
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /**
     * 전화번호 정규식 (한국)
     */
    public static final String PHONE_REGEX = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$";

    /**
     * 비밀번호 최소 길이
     * SECURITY: 8자 이상 (영문+숫자+특수문자 필수)
     */
    public static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * 비밀번호 최대 길이
     */
    public static final int PASSWORD_MAX_LENGTH = 100;

    /**
     * 비밀번호 정규식
     * SECURITY: 8자 이상, 영문자+숫자+특수문자 필수
     * 허용 특수문자: !@#$%^&*()_+\-=[\]{};':"\\|,.<>/?
     */
    public static final String PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";

    /**
     * 사용자명 최소 길이
     */
    public static final int USERNAME_MIN_LENGTH = 3;

    /**
     * 사용자명 최대 길이
     */
    public static final int USERNAME_MAX_LENGTH = 20;

    // ==================== 비즈니스 로직 관련 ====================

    /**
     * 가계부 최대 생성 개수 (사용자당)
     */
    public static final int MAX_LEDGERS_PER_USER = 10;

    /**
     * 거래 내역 최대 조회 기간 (일)
     */
    public static final int MAX_TRANSACTION_QUERY_DAYS = 365;

    /**
     * 예산 최대 금액
     */
    public static final long MAX_BUDGET_AMOUNT = 1_000_000_000L;

    /**
     * 거래 최대 금액
     */
    public static final long MAX_TRANSACTION_AMOUNT = 100_000_000L;

    // ==================== 캐시 관련 ====================

    /**
     * 캐시 기본 TTL (초)
     */
    public static final int CACHE_DEFAULT_TTL = 3600; // 1시간

    /**
     * 캐시 이름 - 사용자
     */
    public static final String CACHE_USER = "users";

    /**
     * 캐시 이름 - 가계부
     */
    public static final String CACHE_LEDGER = "ledgers";

    /**
     * 캐시 이름 - 거래
     */
    public static final String CACHE_TRANSACTION = "transactions";

    // ==================== HTTP 관련 ====================

    /**
     * 사용자 정의 헤더 - Request ID
     */
    public static final String HEADER_REQUEST_ID = "X-Request-ID";

    /**
     * 사용자 정의 헤더 - User ID
     */
    public static final String HEADER_USER_ID = "X-User-ID";

    /**
     * CORS 허용 헤더
     */
    public static final String[] CORS_ALLOWED_HEADERS = {
        "Authorization", "Content-Type", "X-Request-ID", "Refresh-Token"
    };

    /**
     * CORS 허용 메서드
     */
    public static final String[] CORS_ALLOWED_METHODS = {
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    };

    // ==================== 인코딩 관련 ====================

    /**
     * 기본 문자 인코딩
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * JSON Content-Type
     */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * XML Content-Type
     */
    public static final String CONTENT_TYPE_XML = "application/xml";

    // ==================== 기타 ====================

    /**
     * 시스템 사용자 (시스템에 의해 생성된 데이터)
     */
    public static final String SYSTEM_USER = "SYSTEM";

    /**
     * 삭제된 사용자 (탈퇴한 사용자)
     */
    public static final String DELETED_USER = "DELETED";

    /**
     * 기본 언어
     */
    public static final String DEFAULT_LANGUAGE = "ko";

    /**
     * 지원 언어 목록
     */
    public static final String[] SUPPORTED_LANGUAGES = {"ko", "en"};
}
