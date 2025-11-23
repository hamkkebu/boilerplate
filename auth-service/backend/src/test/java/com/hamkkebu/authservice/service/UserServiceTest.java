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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 단위 테스트
 *
 * <p>Mockito를 사용하여 의존성을 격리하고 비즈니스 로직만 테스트합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRequest validUserRequest;
    private User savedUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        // Given: 테스트 데이터 준비
        validUserRequest = UserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("SecureP@ssw0rd123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("010-1234-5678")
                .country("Korea")
                .city("Seoul")
                .state("Gangnam")
                .streetAddress("123 Test St")
                .postalCode("12345")
                .build();

        savedUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$12$encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("010-1234-5678")
                .country("Korea")
                .city("Seoul")
                .state("Gangnam")
                .streetAddress("123 Test St")
                .postalCode("12345")
                .isActive(true)
                .isVerified(false)
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(savedUser, "userId", 1L);

        userResponse = UserResponse.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("010-1234-5678")
                .country("Korea")
                .city("Seoul")
                .state("Gangnam")
                .streetAddress("123 Test St")
                .postalCode("12345")
                .isActive(true)
                .isVerified(false)
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void registerUser_Success() {
        // Given
        when(userRepository.existsByUsername(validUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(false);
        doNothing().when(passwordValidator).validatePasswordFormat(anyString());
        when(passwordEncoder.encode(validUserRequest.getPassword())).thenReturn("$2a$12$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(userResponse);

        // When
        UserResponse result = userService.registerUser(validUserRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordValidator).validatePasswordFormat("SecureP@ssw0rd123");
        verify(passwordEncoder).encode("SecureP@ssw0rd123");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(savedUser);
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 아이디")
    void registerUser_DuplicateUsername() {
        // Given
        when(userRepository.existsByUsername(validUserRequest.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(validUserRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 사용된 아이디입니다")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_RESOURCE);

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 이메일")
    void registerUser_DuplicateEmail() {
        // Given
        when(userRepository.existsByUsername(validUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(validUserRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 사용된 이메일입니다")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_RESOURCE);

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 강도 검증 실패")
    void registerUser_WeakPassword() {
        // Given
        when(userRepository.existsByUsername(validUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(false);
        doThrow(new BusinessException(ErrorCode.VALIDATION_FAILED, "비밀번호는 8자 이상이어야 합니다"))
                .when(passwordValidator).validatePasswordFormat(anyString());

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(validUserRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("비밀번호는 8자 이상이어야 합니다");

        verify(passwordValidator).validatePasswordFormat(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("아이디 중복 확인 - 존재함")
    void checkUsernameDuplicate_Exists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        DuplicateCheckResponse result = userService.checkUsernameDuplicate("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isExists()).isTrue();
        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getValue()).isEqualTo("testuser");

        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    @DisplayName("아이디 중복 확인 - 사용 가능")
    void checkUsernameDuplicate_Available() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        // When
        DuplicateCheckResponse result = userService.checkUsernameDuplicate("newuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isExists()).isFalse();
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getValue()).isEqualTo("newuser");

        verify(userRepository).existsByUsername("newuser");
    }

    @Test
    @DisplayName("이메일 중복 확인 - 존재함")
    void checkEmailDuplicate_Exists() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        DuplicateCheckResponse result = userService.checkEmailDuplicate("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isExists()).isTrue();
        assertThat(result.isAvailable()).isFalse();

        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("이메일 중복 확인 - 사용 가능")
    void checkEmailDuplicate_Available() {
        // Given
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // When
        DuplicateCheckResponse result = userService.checkEmailDuplicate("new@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isExists()).isFalse();
        assertThat(result.isAvailable()).isTrue();

        verify(userRepository).existsByEmail("new@example.com");
    }

    @Test
    @DisplayName("사용자 조회 by userId - 성공")
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(userMapper.toDto(savedUser)).thenReturn(userResponse);

        // When
        UserResponse result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findById(1L);
        verify(userMapper).toDto(savedUser);
    }

    @Test
    @DisplayName("사용자 조회 by userId - 실패 (사용자 없음)")
    void getUserById_NotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findById(999L);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("사용자 조회 by username - 성공")
    void getUserByUsername_Success() {
        // Given
        when(userRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(Optional.of(savedUser));
        when(userMapper.toDto(savedUser)).thenReturn(userResponse);

        // When
        UserResponse result = userService.getUserByUsername("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findByUsernameAndIsDeletedFalse("testuser");
        verify(userMapper).toDto(savedUser);
    }

    @Test
    @DisplayName("사용자 조회 by username - 실패 (사용자 없음)")
    void getUserByUsername_NotFound() {
        // Given
        when(userRepository.findByUsernameAndIsDeletedFalse("unknown")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUsername("unknown"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findByUsernameAndIsDeletedFalse("unknown");
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    void deleteUserByUsername_Success() {
        // Given
        when(userRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("SecureP@ssw0rd123", savedUser.getPasswordHash())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        userService.deleteUserByUsername("testuser", "SecureP@ssw0rd123");

        // Then
        verify(userRepository).findByUsernameAndIsDeletedFalse("testuser");
        verify(passwordEncoder).matches("SecureP@ssw0rd123", savedUser.getPasswordHash());
        verify(userRepository).save(savedUser);
    }

    @Test
    @DisplayName("회원 탈퇴 - 실패 (사용자 없음)")
    void deleteUserByUsername_UserNotFound() {
        // Given
        when(userRepository.findByUsernameAndIsDeletedFalse("unknown")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUserByUsername("unknown", "password"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findByUsernameAndIsDeletedFalse("unknown");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원 탈퇴 - 실패 (비밀번호 불일치)")
    void deleteUserByUsername_WrongPassword() {
        // Given
        when(userRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("wrongPassword", savedUser.getPasswordHash())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUserByUsername("testuser", "wrongPassword"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);

        verify(userRepository).findByUsernameAndIsDeletedFalse("testuser");
        verify(passwordEncoder).matches("wrongPassword", savedUser.getPasswordHash());
        verify(userRepository, never()).save(any());
    }
}
