package com.hamkkebu.authservice.service;

import com.hamkkebu.authservice.controller.AdminController.UserStatsResponse;
import com.hamkkebu.authservice.data.dto.UserResponse;
import com.hamkkebu.authservice.data.entity.User;
import com.hamkkebu.authservice.data.mapper.UserMapper;
import com.hamkkebu.authservice.repository.UserRepository;
import com.hamkkebu.boilerplate.common.enums.Role;
import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자 서비스
 *
 * <p>관리자 전용 기능(사용자 관리, 권한 관리, 통계)을 처리합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * 전체 사용자 목록 조회 (탈퇴한 사용자 제외)
     *
     * @return 전체 사용자 목록
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("관리자 - 전체 사용자 목록 조회");
        List<User> users = userRepository.findByIsDeletedFalse();
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 권한 변경
     *
     * @param username 사용자 아이디
     * @param role 변경할 권한
     * @return 변경된 사용자 정보
     */
    @Transactional
    public UserResponse updateUserRole(String username, Role role) {
        log.info("관리자 - 사용자 권한 변경: username={}, role={}", username, role);

        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateRole(role);
        User updatedUser = userRepository.save(user);

        log.info("사용자 권한 변경 완료: userId={}, username={}, newRole={}",
                updatedUser.getUserId(), updatedUser.getUsername(), updatedUser.getRole());

        return userMapper.toDto(updatedUser);
    }

    /**
     * 사용자 활성화/비활성화 상태 변경
     *
     * @param username 사용자 아이디
     * @param isActive 활성화 여부
     * @return 변경된 사용자 정보
     */
    @Transactional
    public UserResponse updateUserActiveStatus(String username, Boolean isActive) {
        log.info("관리자 - 사용자 활성화 상태 변경: username={}, isActive={}", username, isActive);

        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateActiveStatus(isActive);
        User updatedUser = userRepository.save(user);

        log.info("사용자 활성화 상태 변경 완료: userId={}, username={}, isActive={}",
                updatedUser.getUserId(), updatedUser.getUsername(), updatedUser.getIsActive());

        return userMapper.toDto(updatedUser);
    }

    /**
     * 탈퇴한 사용자 목록 조회
     *
     * @return 탈퇴한 사용자 목록
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getDeletedUsers() {
        log.info("관리자 - 탈퇴한 사용자 목록 조회");
        List<User> users = userRepository.findByIsDeletedTrue();
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 통계 조회
     *
     * @return 사용자 통계 정보
     */
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats() {
        log.info("관리자 - 사용자 통계 조회");

        long totalUsers = userRepository.countByIsDeletedFalse();
        long activeUsers = userRepository.countByIsActiveTrueAndIsDeletedFalse();
        long deletedUsers = userRepository.countByIsDeletedTrue();
        long adminUsers = userRepository.countByRoleAndIsDeletedFalse(Role.ADMIN);
        long developerUsers = userRepository.countByRoleAndIsDeletedFalse(Role.DEVELOPER);

        log.debug("통계 - total={}, active={}, deleted={}, admin={}, developer={}",
                totalUsers, activeUsers, deletedUsers, adminUsers, developerUsers);

        return new UserStatsResponse(
                totalUsers,
                activeUsers,
                deletedUsers,
                adminUsers,
                developerUsers
        );
    }
}
