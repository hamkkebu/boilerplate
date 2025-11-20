DROP DATABASE IF EXISTS hamkkebu_boilerplate_db;
CREATE DATABASE hamkkebu_boilerplate_db;
USE hamkkebu_boilerplate_db;
DROP TABLE IF EXISTS tbl_boilerplate_sample;
CREATE TABLE tbl_boilerplate_sample (
    -- Primary Key
    sample_num BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Business Fields
    -- SECURITY: 조건부 UNIQUE 제약조건으로 Race Condition 방어
    -- is_deleted=false인 행에 대해서만 sample_id가 유일해야 함
    sample_id VARCHAR(20) NOT NULL,
    sample_first_name VARCHAR(50),
    sample_last_name VARCHAR(50),
    sample_nickname VARCHAR(50),
    sample_email VARCHAR(100),
    sample_phone VARCHAR(20),
    sample_password VARCHAR(255) NOT NULL,
    sample_country VARCHAR(50),
    sample_city VARCHAR(50),
    sample_state VARCHAR(50),
    sample_street1 VARCHAR(100),
    sample_street2 VARCHAR(100),
    sample_zip VARCHAR(20),

    -- RBAC: Role-Based Access Control
    sample_role VARCHAR(20) NOT NULL DEFAULT 'USER',

    -- Auditing Fields (from BaseEntity)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),

    -- Soft Delete Fields (from BaseEntity)
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,

    -- Indexes
    INDEX idx_is_deleted (is_deleted),
    INDEX idx_created_at (created_at)
);

-- SECURITY: 조건부 UNIQUE 제약조건 (MySQL 8.0.13+, Functional Index)
-- Race Condition 방어: 동시에 같은 ID로 가입 시도 시 DB 레벨에서 차단
-- is_deleted=false인 레코드에 대해서만 sample_id/sample_nickname가 유일해야 함
--
-- 주의: 재가입 방지 정책
-- - 애플리케이션 레벨에서 탈퇴한 사용자 ID/닉네임 재사용을 차단합니다 (SampleService.createSample)
-- - DB 제약조건은 활성 사용자 간의 중복만 방지 (성능 최적화)
-- - 탈퇴한 사용자(is_deleted=true)의 ID/닉네임은 애플리케이션에서 차단되므로 DB에 도달하지 않음

-- 활성 사용자(is_deleted=false)에 대한 sample_id 유니크 제약
CREATE UNIQUE INDEX idx_sample_id_active
    ON tbl_boilerplate_sample ((CASE WHEN is_deleted = FALSE THEN sample_id END));

-- 활성 사용자(is_deleted=false)에 대한 sample_nickname 유니크 제약
CREATE UNIQUE INDEX idx_sample_nickname_active
    ON tbl_boilerplate_sample ((CASE WHEN is_deleted = FALSE THEN sample_nickname END));

-- 조회 성능을 위한 일반 인덱스
CREATE INDEX idx_sample_id ON tbl_boilerplate_sample(sample_id);
CREATE INDEX idx_sample_nickname ON tbl_boilerplate_sample(sample_nickname);

-- ==========================================
-- Transactional Outbox 테이블
-- ==========================================
DROP TABLE IF EXISTS tbl_outbox_event;
CREATE TABLE tbl_outbox_event (
    -- Primary Key
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Event Information
    event_id VARCHAR(36) NOT NULL UNIQUE,      -- UUID 형태의 이벤트 고유 ID
    event_type VARCHAR(100) NOT NULL,          -- 이벤트 타입 (예: USER_CREATED)
    topic VARCHAR(100) NOT NULL,               -- Kafka 토픽명
    resource_id VARCHAR(100) NOT NULL,         -- 리소스 ID (Kafka 파티션 키)

    -- Event Payload (JSON)
    payload JSON NOT NULL,                     -- 전체 이벤트 객체를 JSON으로 직렬화

    -- Status Management
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, PUBLISHED, FAILED
    retry_count INT NOT NULL DEFAULT 0,        -- 재시도 횟수
    max_retry INT NOT NULL DEFAULT 3,          -- 최대 재시도 횟수

    -- Error Tracking
    error_message TEXT,                        -- 실패 시 에러 메시지

    -- Timestamps
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME,                     -- 발행 완료 시각
    last_retry_at DATETIME,                    -- 마지막 재시도 시각

    -- Indexes
    INDEX idx_status_created (status, created_at),
    INDEX idx_event_id (event_id),
    INDEX idx_topic (topic)
) COMMENT='Transactional Outbox 이벤트 테이블 - DB 트랜잭션과 이벤트 발행의 원자성 보장';