package com.hamkkebu.authservice.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 비밀번호 해시 생성 유틸리티
 *
 * data.sql에서 사용할 BCrypt 해시를 생성합니다.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        // SecurityConfig와 동일한 strength 12 사용
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        System.out.println("\n=== BCrypt Password Hashes (Strength 12) ===\n");

        // 초기 계정 비밀번호들 생성
        String[][] accounts = {
            {"k1m743hyun", "temp_password_123"},
            {"admin", "admin123"},
            {"testuser", "test123"},
            {"demo", "demo123"}
        };

        for (String[] account : accounts) {
            String username = account[0];
            String password = account[1];
            String encoded = encoder.encode(password);

            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            System.out.println("BCrypt:   " + encoded);
            System.out.println();
        }

        System.out.println("===========================================\n");
    }
}
