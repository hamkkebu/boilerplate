DROP DATABASE IF EXISTS hamkkebu_boilerplate_db;
CREATE DATABASE hamkkebu_boilerplate_db;
USE hamkkebu_boilerplate_db;
DROP TABLE IF EXISTS tbl_boilerplate_sample;
CREATE TABLE tbl_boilerplate_sample (
    sample_num BIGINT PRIMARY KEY AUTO_INCREMENT,
    sample_id VARCHAR(20) UNIQUE NOT NULL,
    sample_fname VARCHAR(20),
    sample_lname VARCHAR(20)
);