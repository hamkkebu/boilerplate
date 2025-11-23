package com.hamkkebu.authservice.service;

import com.hamkkebu.authservice.data.dto.LoginRequest;
import com.hamkkebu.authservice.data.dto.LoginResponse;
import com.hamkkebu.authservice.data.dto.TokenResponse;
import com.hamkkebu.authservice.data.dto.UserResponse;
import com.hamkkebu.authservice.data.entity.User;
import com.hamkkebu.authservice.data.mapper.UserMapper;
import com.hamkkebu.authservice.repository.UserRepository;
import com.hamkkebu.boilerplate.common.enums.Role;
import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import com.hamkkebu.boilerplate.common.security.JwtTokenProvider;
import com.hamkkebu.boilerplate.common.security.LoginAttemptService;
import com.hamkkebu.boilerplate.common.security.RefreshTokenWhitelistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenWhitelistService refreshTokenWhitelistService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        // refreshTokenValidity 설정 (7일)
        ReflectionTestUtils.setField(authService, "refreshTokenValidity", 7L * 24 * 60 * 60 * 1000);

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$12$encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .isVerified(true)
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "userId", 1L);

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("SecureP@ssw0rd123")
                .build();

        userResponse = UserResponse.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .isVerified(true)
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // Given
        when(loginAttemptService.isBlocked("testuser")).thenReturn(false);
        when(userRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("SecureP@ssw0rd123", testUser.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.createAccessToken("testuser", "ROLE_USER")).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken("testuser", "ROLE_USER")).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpirationDate("access-token")).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        when(userMapper.toDto(testUser)).thenReturn(userResponse);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        LoginResponse result = authService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getToken().getAccessToken()).isEqualTo("access-token");
        assertThat(result.getToken().getRefreshToken()).isEqualTo("refresh-token");

        verify(loginAttemptService).isBlocked("testuser");
        verify(loginAttemptService).resetLoginAttempts("testuser");
        verify(userRepository).findByUsernameAndIsDeletedFalse("testuser");
        verify(passwordEncoder).matches("SecureP@ssw0rd123", testUser.getPasswordHash());
        verify(jwtTokenProvider).createAccessToken("testuser", "ROLE_USER");
        verify(jwtTokenProvider).createRefreshToken("testuser", "ROLE_USER");
        verify(refreshTokenWhitelistService).addToWhitelist("testuser", "refresh-token", 7L * 24 * 60 * 60 * 1000);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("로그인 실패 - 로그인 차단")
    void login_Blocked() {
        // Given
        when(loginAttemptService.isBlocked("testuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("로그인 시도 횟수가 초과되었습니다")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);

        verify(loginAttemptService).isBlocked("testuser");
        verify(userRepository, never()).findByUsernameAndIsDeletedFalse(anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void login_UserNotFound() {
        // Given
        when(loginAttemptService.isBlocked("unknown")).thenReturn(false);
        when(userRepository.findByUsernameAndIsDeletedFalse("unknown")).thenReturn(Optional.empty());
        when(loginAttemptService.getRemainingAttempts("unknown")).thenReturn(4);

        LoginRequest invalidRequest = LoginRequest.builder()
                .username("unknown")
                .password("password")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.login(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("등록되지 않은 아이디입니다")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(loginAttemptService).recordLoginAttempt("unknown");
        verify(passwordEncoder).matches(anyString(), anyString()); // Timing attack 방어
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_WrongPassword() {
        // Given
        when(loginAttemptService.isBlocked("testuser")).thenReturn(false);
        when(userRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPasswordHash())).thenReturn(false);
        when(loginAttemptService.getRemainingAttempts("testuser")).thenReturn(4);

        LoginRequest wrongPasswordRequest = LoginRequest.builder()
                .username("testuser")
                .password("wrongPassword")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.login(wrongPasswordRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);

        verify(loginAttemptService).recordLoginAttempt("testuser");
        verify(loginAttemptService).getRemainingAttempts("testuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("토큰 갱신 성공 (Refresh Token Rotation)")
    void refresh_Success() {
        // Given
        String oldRefreshToken = "old-refresh-token";
        when(jwtTokenProvider.validateToken(oldRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserId(oldRefreshToken)).thenReturn("testuser");
        when(refreshTokenWhitelistService.isWhitelisted("testuser", oldRefreshToken)).thenReturn(true);
        when(userRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.createAccessToken("testuser", "ROLE_USER")).thenReturn("new-access-token");
        when(jwtTokenProvider.createRefreshToken("testuser", "ROLE_USER")).thenReturn("new-refresh-token");
        when(jwtTokenProvider.getExpirationDate("new-access-token")).thenReturn(new Date(System.currentTimeMillis() + 3600000));

        // When
        TokenResponse result = authService.refresh(oldRefreshToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");

        verify(jwtTokenProvider).validateToken(oldRefreshToken);
        verify(refreshTokenWhitelistService).isWhitelisted("testuser", oldRefreshToken);
        verify(refreshTokenWhitelistService).removeFromWhitelist("testuser"); // 기존 토큰 무효화
        verify(refreshTokenWhitelistService).addToWhitelist("testuser", "new-refresh-token", 7L * 24 * 60 * 60 * 1000);
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
    void refresh_InvalidToken() {
        // Given
        String invalidToken = "invalid-token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refresh(invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰입니다")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);

        verify(jwtTokenProvider).validateToken(invalidToken);
        verify(refreshTokenWhitelistService, never()).isWhitelisted(anyString(), anyString());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - Whitelist에 없는 토큰")
    void refresh_NotInWhitelist() {
        // Given
        String refreshToken = "refresh-token";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserId(refreshToken)).thenReturn("testuser");
        when(refreshTokenWhitelistService.isWhitelisted("testuser", refreshToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("유효하지 않거나 로그아웃된 리프레시 토큰입니다")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);

        verify(refreshTokenWhitelistService).isWhitelisted("testuser", refreshToken);
        verify(userRepository, never()).findByUsernameAndIsDeletedFalse(anyString());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 사용자 없음")
    void refresh_UserNotFound() {
        // Given
        String refreshToken = "refresh-token";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserId(refreshToken)).thenReturn("deleteduser");
        when(refreshTokenWhitelistService.isWhitelisted("deleteduser", refreshToken)).thenReturn(true);
        when(userRepository.findByUsernameAndIsDeletedFalse("deleteduser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(userRepository).findByUsernameAndIsDeletedFalse("deleteduser");
    }

    @Test
    @DisplayName("토큰 검증 - 유효한 토큰")
    void validateToken_Valid() {
        // Given
        String validToken = "valid-token";
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);

        // When
        boolean result = authService.validateToken(validToken);

        // Then
        assertThat(result).isTrue();
        verify(jwtTokenProvider).validateToken(validToken);
    }

    @Test
    @DisplayName("토큰 검증 - 유효하지 않은 토큰")
    void validateToken_Invalid() {
        // Given
        String invalidToken = "invalid-token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // When
        boolean result = authService.validateToken(invalidToken);

        // Then
        assertThat(result).isFalse();
        verify(jwtTokenProvider).validateToken(invalidToken);
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 - 성공")
    void getUserIdFromToken_Success() {
        // Given
        String token = "valid-token";
        when(jwtTokenProvider.getUserId(token)).thenReturn("testuser");

        // When
        String result = authService.getUserIdFromToken(token);

        // Then
        assertThat(result).isEqualTo("testuser");
        verify(jwtTokenProvider).getUserId(token);
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 - 실패")
    void getUserIdFromToken_Failure() {
        // Given
        String token = "invalid-token";
        when(jwtTokenProvider.getUserId(token)).thenThrow(new RuntimeException("Invalid token"));

        // When & Then
        assertThatThrownBy(() -> authService.getUserIdFromToken(token))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("유효하지 않은 토큰입니다")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // Given
        String refreshToken = "refresh-token";
        when(jwtTokenProvider.getUserId(refreshToken)).thenReturn("testuser");

        // When
        authService.logout(refreshToken);

        // Then
        verify(jwtTokenProvider).getUserId(refreshToken);
        verify(refreshTokenWhitelistService).removeFromWhitelist("testuser");
    }

    @Test
    @DisplayName("로그아웃 - 토큰 없음 (무시)")
    void logout_NoToken() {
        // When
        authService.logout(null);

        // Then
        verify(jwtTokenProvider, never()).getUserId(anyString());
        verify(refreshTokenWhitelistService, never()).removeFromWhitelist(anyString());
    }

    @Test
    @DisplayName("로그아웃 - 빈 토큰 (무시)")
    void logout_EmptyToken() {
        // When
        authService.logout("");

        // Then
        verify(jwtTokenProvider, never()).getUserId(anyString());
        verify(refreshTokenWhitelistService, never()).removeFromWhitelist(anyString());
    }
}
