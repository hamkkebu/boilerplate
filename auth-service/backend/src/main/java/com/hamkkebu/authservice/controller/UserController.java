package com.hamkkebu.authservice.controller;

import com.hamkkebu.authservice.data.dto.DeleteUserRequest;
import com.hamkkebu.authservice.data.dto.DuplicateCheckResponse;
import com.hamkkebu.authservice.data.dto.UserRequest;
import com.hamkkebu.authservice.data.dto.UserResponse;
import com.hamkkebu.authservice.service.UserService;
import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * User API 컨트롤러
 *
 * <p>사용자 등록, 조회, 중복 확인 등의 API를 제공합니다.</p>
 */
@Tag(name = "User API", description = "사용자 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 등록 (회원가입)
     *
     * <p>새로운 사용자를 등록합니다.</p>
     * <p>아이디와 이메일 중복을 확인하고, 비밀번호를 암호화하여 저장합니다.</p>
     *
     * @param request 사용자 등록 요청
     * @return 등록된 사용자 정보
     */
    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다. 아이디와 이메일 중복을 확인합니다.",
        security = {} // 인증 불필요
    )
    @PostMapping
    public ApiResponse<UserResponse> registerUser(@Valid @RequestBody UserRequest request) {
        log.info("회원가입 요청: username={}", request.getUsername());
        UserResponse response = userService.registerUser(request);
        return ApiResponse.success(response, "회원가입이 완료되었습니다");
    }

    /**
     * 아이디 중복 확인
     *
     * @param username 확인할 아이디
     * @return 중복 확인 결과
     */
    @Operation(
        summary = "아이디 중복 확인",
        description = "사용자 아이디의 중복 여부를 확인합니다.",
        security = {} // 인증 불필요
    )
    @GetMapping("/check/{username}")
    public ApiResponse<DuplicateCheckResponse> checkUsernameDuplicate(@PathVariable String username) {
        log.debug("아이디 중복 확인: username={}", username);
        DuplicateCheckResponse response = userService.checkUsernameDuplicate(username);
        return ApiResponse.success(response);
    }

    /**
     * 닉네임 중복 확인 (username과 동일하게 처리)
     *
     * <p>현재는 nickname 필드가 없으므로 username 중복 확인으로 처리합니다.</p>
     *
     * @param nickname 확인할 닉네임
     * @return 중복 확인 결과
     */
    @Operation(
        summary = "닉네임 중복 확인",
        description = "닉네임의 중복 여부를 확인합니다. (현재는 username과 동일하게 처리)",
        security = {} // 인증 불필요
    )
    @GetMapping("/check/nickname/{nickname}")
    public ApiResponse<DuplicateCheckResponse> checkNicknameDuplicate(@PathVariable String nickname) {
        log.debug("닉네임 중복 확인: nickname={}", nickname);
        // 현재는 nickname 필드가 없으므로 username 중복 확인으로 처리
        DuplicateCheckResponse response = userService.checkUsernameDuplicate(nickname);
        return ApiResponse.success(response);
    }

    /**
     * 이메일 중복 확인
     *
     * @param email 확인할 이메일
     * @return 중복 확인 결과
     */
    @Operation(
        summary = "이메일 중복 확인",
        description = "이메일의 중복 여부를 확인합니다.",
        security = {} // 인증 불필요
    )
    @GetMapping("/check/email/{email}")
    public ApiResponse<DuplicateCheckResponse> checkEmailDuplicate(@PathVariable String email) {
        log.debug("이메일 중복 확인: email={}", email);
        DuplicateCheckResponse response = userService.checkEmailDuplicate(email);
        return ApiResponse.success(response);
    }

    /**
     * 사용자 조회 by userId
     *
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    @Operation(
        summary = "사용자 조회 (ID)",
        description = "사용자 ID로 사용자 정보를 조회합니다."
    )
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long userId) {
        log.debug("사용자 조회: userId={}", userId);
        UserResponse response = userService.getUserById(userId);
        return ApiResponse.success(response);
    }

    /**
     * 사용자 조회 by username
     *
     * @param username 사용자 아이디
     * @return 사용자 정보
     */
    @Operation(
        summary = "사용자 조회 (username)",
        description = "사용자 아이디로 사용자 정보를 조회합니다."
    )
    @GetMapping("/username/{username}")
    public ApiResponse<UserResponse> getUserByUsername(@PathVariable String username) {
        log.debug("사용자 조회: username={}", username);
        UserResponse response = userService.getUserByUsername(username);
        return ApiResponse.success(response);
    }

    /**
     * 사용자 삭제 (회원 탈퇴) by username
     *
     * @param username 사용자 아이디
     * @param request 삭제 요청 (비밀번호 포함)
     * @return 성공 메시지
     */
    @Operation(
        summary = "회원 탈퇴",
        description = "사용자 아이디로 회원을 탈퇴합니다. 비밀번호 확인이 필요합니다."
    )
    @DeleteMapping("/username/{username}")
    public ApiResponse<Void> deleteUserByUsername(
            @PathVariable String username,
            @Valid @RequestBody DeleteUserRequest request) {
        log.info("회원 탈퇴 요청: username={}", username);
        userService.deleteUserByUsername(username, request.getPassword());
        return ApiResponse.success(null, "회원 탈퇴가 완료되었습니다");
    }
}
