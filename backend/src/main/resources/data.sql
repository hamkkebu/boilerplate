INSERT INTO tbl_boilerplate_sample (
    sample_id, sample_fname, sample_lname, sample_nickname, sample_email, sample_phone,
    sample_country, sample_city, sample_state, sample_street1, sample_zip,
    created_at, updated_at, created_by, updated_by, deleted
)
VALUES (
    'k1m743hyun', 'Taehyun', 'Kim', 'k1m', 'k1m743hyun@example.com', '010-1234-5678',
    'South Korea', 'Seoul', 'Seoul', '123 Gangnam-daero', '06000',
    NOW(), NOW(), 'system', 'system', FALSE
);