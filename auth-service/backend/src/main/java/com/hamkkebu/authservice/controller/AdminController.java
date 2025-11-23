package com.hamkkebu.authservice.controller;

import com.hamkkebu.authservice.data.dto.UserResponse;
import com.hamkkebu.authservice.service.AdminService;
import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import com.hamkkebu.boilerplate.common.enums.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 전용 API Controller
 *
 * <p>관리자(ADMIN, DEVELOPER) 권한이 필요한 엔드포인트입니다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin API", description = "관리자 전용 API")
public class AdminController {

    private final AdminService adminService;

    /**
     * 전체 사용자 목록 조회 (관리자 전용)
     *
     * @return 전체 사용자 목록
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(
            summary = "전체 사용자 목록 조회",
            description = "관리자 권한으로 전체 사용자 목록을 조회합니다. (탈퇴한 사용자 제외)"
    )
    public ApiResponse<List<UserResponse>> getAllUsers() {
        log.info("관리자 - 전체 사용자 목록 조회");
        List<UserResponse> users = adminService.getAllUsers();
        return ApiResponse.success(users);
    }

    /**
     * 사용자 권한 변경 (관리자 전용)
     *
     * @param username 사용자 아이디
     * @param role 변경할 권한
     * @return 변경된 사용자 정보
     */
    @PutMapping("/users/{username}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "사용자 권한 변경",
            description = "관리자 권한으로 사용자의 권한을 변경합니다. (ADMIN 권한 필요)"
    )
    public ApiResponse<UserResponse> updateUserRole(
            @Parameter(description = "사용자 아이디", required = true)
            @PathVariable String username,
            @Parameter(description = "변경할 권한 (USER, ADMIN, DEVELOPER)", required = true)
            @RequestParam Role role
    ) {
        log.info("관리자 - 사용자 권한 변경: username={}, role={}", username, role);
        UserResponse user = adminService.updateUserRole(username, role);
        return ApiResponse.success(user, "사용자 권한이 변경되었습니다");
    }

    /**
     * 사용자 활성화/비활성화 (관리자 전용)
     *
     * @param username 사용자 아이디
     * @param isActive 활성화 여부
     * @return 변경된 사용자 정보
     */
    @PutMapping("/users/{username}/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "사용자 활성화/비활성화",
            description = "관리자 권한으로 사용자 계정을 활성화 또는 비활성화합니다."
    )
    public ApiResponse<UserResponse> updateUserActiveStatus(
            @Parameter(description = "사용자 아이디", required = true)
            @PathVariable String username,
            @Parameter(description = "활성화 여부", required = true)
            @RequestParam Boolean isActive
    ) {
        log.info("관리자 - 사용자 활성화 상태 변경: username={}, isActive={}", username, isActive);
        UserResponse user = adminService.updateUserActiveStatus(username, isActive);
        return ApiResponse.success(user, "사용자 활성화 상태가 변경되었습니다");
    }

    /**
     * 탈퇴한 사용자 목록 조회 (관리자 전용)
     *
     * @return 탈퇴한 사용자 목록
     */
    @GetMapping("/users/deleted")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(
            summary = "탈퇴한 사용자 목록 조회",
            description = "관리자 권한으로 탈퇴한 사용자 목록을 조회합니다."
    )
    public ApiResponse<List<UserResponse>> getDeletedUsers() {
        log.info("관리자 - 탈퇴한 사용자 목록 조회");
        List<UserResponse> users = adminService.getDeletedUsers();
        return ApiResponse.success(users);
    }

    /**
     * 사용자 통계 조회 (관리자 전용)
     *
     * @return 사용자 통계 정보
     */
    @GetMapping("/users/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(
            summary = "사용자 통계 조회",
            description = "전체 사용자, 활성 사용자, 탈퇴 사용자 등의 통계를 조회합니다."
    )
    public ApiResponse<UserStatsResponse> getUserStats() {
        log.info("관리자 - 사용자 통계 조회");
        UserStatsResponse stats = adminService.getUserStats();
        return ApiResponse.success(stats);
    }

    /**
     * 사용자 통계 응답 DTO
     */
    public record UserStatsResponse(
            long totalUsers,
            long activeUsers,
            long deletedUsers,
            long adminUsers,
            long developerUsers
    ) {}
}
