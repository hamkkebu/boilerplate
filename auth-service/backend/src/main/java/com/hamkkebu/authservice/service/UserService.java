package com.hamkkebu.authservice.service;

import com.hamkkebu.authservice.data.dto.DuplicateCheckResponse;
import com.hamkkebu.authservice.data.dto.UserRequest;
import com.hamkkebu.authservice.data.dto.UserResponse;
import com.hamkkebu.authservice.data.entity.User;
import com.hamkkebu.authservice.data.mapper.UserMapper;
import com.hamkkebu.authservice.repository.UserRepository;
import com.hamkkebu.boilerplate.common.enums.Role;
import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import com.hamkkebu.boilerplate.common.security.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User 서비스
 *
 * <p>사용자 등록, 조회, 중복 확인 등의 비즈니스 로직을 처리합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordValidator passwordValidator;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 등록 (회원가입)
     *
     * @param request 사용자 등록 요청
     * @return 등록된 사용자 정보
     */
    @Transactional
    public UserResponse registerUser(UserRequest request) {
        log.info("사용자 등록 시작: username={}", request.getUsername());

        // 아이디 중복 확인 (탈퇴한 회원 포함)
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("중복된 아이디 (탈퇴 회원 포함): {}", request.getUsername());
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용된 아이디입니다 (탈퇴한 회원 포함)");
        }

        // 이메일 중복 확인 (탈퇴한 회원 포함)
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("중복된 이메일 (탈퇴 회원 포함): {}", request.getEmail());
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용된 이메일입니다 (탈퇴한 회원 포함)");
        }

        // SECURITY: 비밀번호 강도 검증
        passwordValidator.validatePasswordFormat(request.getPassword());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 엔티티 생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .country(request.getCountry())
                .city(request.getCity())
                .state(request.getState())
                .streetAddress(request.getStreetAddress())
                .postalCode(request.getPostalCode())
                .isActive(true)
                .isVerified(false)
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 등록 완료: userId={}, username={}", savedUser.getUserId(), savedUser.getUsername());

        return userMapper.toDto(savedUser);
    }

    /**
     * 사용자 아이디 중복 확인 (탈퇴한 회원 포함)
     *
     * @param username 확인할 아이디
     * @return 중복 확인 결과
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkUsernameDuplicate(String username) {
        boolean exists = userRepository.existsByUsername(username);
        log.debug("아이디 중복 확인 (탈퇴 회원 포함): username={}, exists={}", username, exists);
        return DuplicateCheckResponse.of(exists, username);
    }

    /**
     * 사용자 이메일 중복 확인 (탈퇴한 회원 포함)
     *
     * @param email 확인할 이메일
     * @return 중복 확인 결과
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkEmailDuplicate(String email) {
        boolean exists = userRepository.existsByEmail(email);
        log.debug("이메일 중복 확인 (탈퇴 회원 포함): email={}, exists={}", email, exists);
        return DuplicateCheckResponse.of(exists, email);
    }

    /**
     * 사용자 조회 by userId
     *
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toDto(user);
    }

    /**
     * 사용자 조회 by username
     *
     * @param username 사용자 아이디
     * @return 사용자 정보
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toDto(user);
    }

    /**
     * 사용자 삭제 (회원 탈퇴) by username
     *
     * @param username 사용자 아이디
     * @param password 비밀번호 (확인용)
     */
    @Transactional
    public void deleteUserByUsername(String username, String password) {
        log.info("사용자 탈퇴 시작: username={}", username);

        // 사용자 조회
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("비밀번호 불일치: username={}", username);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "비밀번호가 일치하지 않습니다");
        }

        // Soft Delete
        user.delete();
        userRepository.save(user);
        log.info("사용자 탈퇴 완료: username={}", username);
    }

}
