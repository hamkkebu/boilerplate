DROP DATABASE IF EXISTS hamkkebu_boilerplate_db;
CREATE DATABASE hamkkebu_boilerplate_db;
USE hamkkebu_boilerplate_db;
DROP TABLE IF EXISTS tbl_boilerplate_sample;
CREATE TABLE tbl_boilerplate_sample (
    -- Primary Key
    sample_num BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Business Fields
    -- UNIQUE 제약조건 제거: soft delete 사용 시 동일 ID로 재가입 허용
    -- 중복 검증은 애플리케이션 레벨에서 deleted=false만 체크
    sample_id VARCHAR(20) NOT NULL,
    sample_fname VARCHAR(50),
    sample_lname VARCHAR(50),
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

    -- Auditing Fields (from BaseEntity)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),

    -- Soft Delete Fields (from BaseEntity)
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,

    -- Indexes
    INDEX idx_sample_id (sample_id),
    INDEX idx_deleted (deleted),
    INDEX idx_created_at (created_at),

    -- Soft Delete를 고려한 복합 인덱스
    -- 활성 사용자(deleted=false)에 대한 sample_id 중복 검증 성능 향상
    INDEX idx_sample_id_deleted (sample_id, deleted)
);