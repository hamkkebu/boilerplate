DROP DATABASE IF EXISTS hamkkebu_boilerplate_db;
CREATE DATABASE hamkkebu_boilerplate_db;
USE hamkkebu_boilerplate_db;
DROP TABLE IF EXISTS tbl_boilerplate_sample;
CREATE TABLE tbl_boilerplate_sample (
    -- Primary Key
    sample_num BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Business Fields
    sample_id VARCHAR(20) UNIQUE NOT NULL,
    sample_fname VARCHAR(50),
    sample_lname VARCHAR(50),
    sample_nickname VARCHAR(50),
    sample_email VARCHAR(100),
    sample_phone VARCHAR(20),
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
    INDEX idx_created_at (created_at)
);