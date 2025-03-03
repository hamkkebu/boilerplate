DROP DATABASE IF EXISTS taste_sample_db;
CREATE DATABASE taste_sample_db;
USE taste_sample_db;
DROP TABLE IF EXISTS tbl_sample_taste;
CREATE TABLE tbl_sample_taste (
    taste_sample_num BIGINT PRIMARY KEY AUTO_INCREMENT,
    taste_sample_id VARCHAR(20) UNIQUE NOT NULL,
    taste_sample_fname VARCHAR(20),
    taste_sample_lname VARCHAR(20)
);