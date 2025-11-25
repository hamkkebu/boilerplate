-- 테스트 계정: k1m743hyun / password123
-- BCrypt 해시는 Spring Security BCryptPasswordEncoder로 생성
INSERT INTO tbl_boilerplate_sample (
    sample_id, sample_first_name, sample_last_name, sample_nickname, sample_email, sample_phone, sample_password,
    sample_country, sample_city, sample_state, sample_street1, sample_zip
)
VALUES (
    'k1m743hyun', 'Taehyun', 'Kim', 'k1m', 'k1m743hyun@example.com', '010-1234-5678',
    '$2a$10$0Cir0Qud/FmDTCJhZAmf3ewQnTpGrtZgky2mOgl43XYIwIDlNjzGC',
    'South Korea', 'Seoul', 'Seoul', '123 Gangnam-daero', '06000'
);