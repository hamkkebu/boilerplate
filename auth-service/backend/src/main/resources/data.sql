-- Insert sample users (for development only)
-- Passwords are hashed using BCrypt (strength 12)
-- These hashes were generated using the actual BCryptPasswordEncoder
-- Plain passwords:
--   k1m743hyun: temp_password_123
--   admin: admin123
--   testuser: test123
--   demo: demo123

INSERT INTO users (username, email, password_hash, first_name, last_name, phone_number, country, city, state, street_address, postal_code, is_active, is_verified, role, created_at, updated_at) VALUES
('k1m743hyun', 'k1m743hyun@hamkkebu.com', '$2a$12$PwDtXWWCMzeZquigkXbOMu7NW35WvZMtNiQyxiQWBAk4tx9lABcpq', 'Taehyun', 'Kim', '+82 10-1234-5678', '대한민국', '수원시', '경기도', '영통구 도청로', '16506', TRUE, TRUE, 'ADMIN', NOW(), NOW()),
('admin', 'admin@hamkkebu.com', '$2a$12$v6Eiy2TaGS.KLTbhXuhcseUdrTlfcCgUOV8W6WI8fAmTMa3IvNKzi', 'Admin', 'User', '010-1234-5678', 'South Korea', 'Seoul', 'Seoul', 'Gangnam-gu', '06000', TRUE, TRUE, 'ADMIN', NOW(), NOW()),
('testuser', 'test@hamkkebu.com', '$2a$12$OJr2mrKe0DUxUcLb2wgTbOj.dAq2GtTa4AItXcCsKvSffFXo5ICS.', 'Test', 'User', '010-9876-5432', 'South Korea', 'Busan', 'Busan', 'Haeundae-gu', '48000', TRUE, FALSE, 'USER', NOW(), NOW()),
('demo', 'demo@hamkkebu.com', '$2a$12$VM.5iBQN/pT/upMeK9mGKedPiUoLnDNmmVe4WLlOopVX8RaBn5dzO', 'Demo', 'User', '010-5555-6666', 'South Korea', 'Incheon', 'Incheon', 'Yeonsu-gu', '21900', TRUE, TRUE, 'DEVELOPER', NOW(), NOW());
