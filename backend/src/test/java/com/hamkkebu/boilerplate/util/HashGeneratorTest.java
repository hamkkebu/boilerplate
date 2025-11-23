package com.hamkkebu.boilerplate.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGeneratorTest {

    @Test
    void generateBCryptHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        // 새로운 비밀번호 정책: 12자 이상, 영문+숫자+특수문자
        String newPassword = "Password123!";
        String newHash = encoder.encode(newPassword);

        System.out.println("====================================");
        System.out.println("BCrypt hash (12 rounds) for '" + newPassword + "':");
        System.out.println(newHash);
        System.out.println("====================================");

        // Verify it works
        boolean newMatches = encoder.matches(newPassword, newHash);
        System.out.println("Verification result: " + newMatches);
        System.out.println("====================================");

        // 기존 비밀번호 (테스트용)
        String oldPassword = "password123";
        String oldHash = encoder.encode(oldPassword);
        System.out.println("OLD - BCrypt hash (12 rounds) for '" + oldPassword + "':");
        System.out.println(oldHash);
        boolean oldMatches = encoder.matches(oldPassword, oldHash);
        System.out.println("OLD - Verification result: " + oldMatches);
        System.out.println("====================================");
    }
}
