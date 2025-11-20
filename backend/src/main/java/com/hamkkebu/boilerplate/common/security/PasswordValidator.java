package com.hamkkebu.boilerplate.common.security;

import com.hamkkebu.boilerplate.common.constant.CommonConstants;
import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 비밀번호 검증 유틸리티
 *
 * <p>비밀번호 검증 로직을 중앙화하여 일관된 검증 및 에러 처리를 제공합니다.</p>
 * <p>주요 기능:</p>
 * <ul>
 *   <li>비밀번호 형식 검증 (길이, 복잡도)</li>
 *   <li>비밀번호 일치 여부 검증</li>
 *   <li>검증 실패 시 로깅 및 예외 발생</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordValidator {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(CommonConstants.PASSWORD_REGEX);

    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 검증
     *
     * <p>입력된 평문 비밀번호와 암호화된 비밀번호를 비교합니다.</p>
     * <p>검증 실패 시 로그를 남기고 BusinessException을 발생시킵니다.</p>
     *
     * @param rawPassword 평문 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @param userId 사용자 ID (로깅용, null 가능)
     * @throws BusinessException 비밀번호가 일치하지 않는 경우
     */
    public void validatePassword(String rawPassword, String encodedPassword, String userId) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            if (userId != null) {
                log.warn("Invalid password for user: {}", userId);
            }
            throw new BusinessException(
                ErrorCode.AUTHENTICATION_FAILED,
                "비밀번호가 일치하지 않습니다"
            );
        }
    }

    /**
     * 비밀번호 검증 (간단한 버전)
     *
     * @param rawPassword 평문 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @throws BusinessException 비밀번호가 일치하지 않는 경우
     */
    public void validatePassword(String rawPassword, String encodedPassword) {
        validatePassword(rawPassword, encodedPassword, null);
    }

    /**
     * 비밀번호 형식 검증
     *
     * <p>비밀번호는 다음 조건을 만족해야 합니다:</p>
     * <ul>
     *   <li>{@link CommonConstants#PASSWORD_MIN_LENGTH}자 이상</li>
     *   <li>영문자 포함 (대소문자 구분 없음)</li>
     *   <li>숫자 포함</li>
     *   <li>특수문자 포함</li>
     * </ul>
     *
     * @param password 검증할 비밀번호
     * @throws BusinessException 비밀번호 형식이 올바르지 않은 경우
     */
    public void validatePasswordFormat(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "비밀번호는 필수 입력 항목입니다"
            );
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                String.format("비밀번호는 %d자 이상이며 영문자, 숫자, 특수문자를 모두 포함해야 합니다",
                    CommonConstants.PASSWORD_MIN_LENGTH)
            );
        }

        log.debug("Password format validation passed");
    }
}
