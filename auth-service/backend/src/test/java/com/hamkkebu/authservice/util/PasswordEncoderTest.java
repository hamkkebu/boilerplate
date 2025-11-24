package com.hamkkebu.authservice.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 비밀번호 인코딩 테스트
 *
 * data.sql에서 사용할 BCrypt 해시를 생성하기 위한 유틸리티 테스트
 */
public class PasswordEncoderTest {

    @Test
    public void generateBCryptPasswords() {
        // SecurityConfig와 동일한 strength 12 사용
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        System.out.println("\n=== BCrypt Password Hashes (Strength 12) ===\n");

        // 초기 계정 비밀번호들 생성
        String[] passwords = {
            "temp_password_123",  // k1m743hyun
            "admin123",           // admin
            "test123",            // testuser
            "demo123"             // demo
        };

        String[] usernames = {
            "k1m743hyun",
            "admin",
            "testuser",
            "demo"
        };

        for (int i = 0; i < passwords.length; i++) {
            String encoded = encoder.encode(passwords[i]);
            System.out.println("Username: " + usernames[i]);
            System.out.println("Password: " + passwords[i]);
            System.out.println("BCrypt:   " + encoded);
            System.out.println();
        }

        System.out.println("===========================================\n");
    }
}
