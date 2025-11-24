package com.hamkkebu.authservice.grpc.server;

import com.hamkkebu.authservice.data.entity.User;
import com.hamkkebu.authservice.grpc.user.*;
import com.hamkkebu.authservice.repository.UserRepository;
import com.hamkkebu.boilerplate.common.enums.Role;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserGrpcService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserGrpcService 테스트")
class UserGrpcServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserGrpcService userGrpcService;

    @Mock
    private StreamObserver<GetUserResponse> getUserResponseObserver;

    @Mock
    private StreamObserver<GetUsersResponse> getUsersResponseObserver;

    @Mock
    private StreamObserver<UserExistsResponse> userExistsResponseObserver;

    private User testUser;

    @BeforeEach
    void setUp() {
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
        ReflectionTestUtils.setField(testUser, "createdAt", LocalDateTime.of(2024, 1, 1, 0, 0));
        ReflectionTestUtils.setField(testUser, "updatedAt", LocalDateTime.of(2024, 1, 1, 0, 0));
    }

    @Test
    @DisplayName("getUser - 사용자 조회 성공")
    void getUser_Success() {
        // Given
        GetUserRequest request = GetUserRequest.newBuilder()
                .setUserId("testuser")
                .build();

        when(userRepository.findByUsernameAndIsDeletedFalse("testuser"))
                .thenReturn(Optional.of(testUser));

        ArgumentCaptor<GetUserResponse> responseCaptor = ArgumentCaptor.forClass(GetUserResponse.class);

        // When
        userGrpcService.getUser(request, getUserResponseObserver);

        // Then
        verify(getUserResponseObserver).onNext(responseCaptor.capture());
        verify(getUserResponseObserver).onCompleted();

        GetUserResponse response = responseCaptor.getValue();
        assertThat(response.hasUser()).isTrue();
        assertThat(response.getUser().getId()).isEqualTo("1");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUser().getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.getErrorMessage()).isEmpty();
    }

    @Test
    @DisplayName("getUser - 사용자 없음")
    void getUser_NotFound() {
        // Given
        GetUserRequest request = GetUserRequest.newBuilder()
                .setUserId("nonexistent")
                .build();

        when(userRepository.findByUsernameAndIsDeletedFalse("nonexistent"))
                .thenReturn(Optional.empty());

        ArgumentCaptor<GetUserResponse> responseCaptor = ArgumentCaptor.forClass(GetUserResponse.class);

        // When
        userGrpcService.getUser(request, getUserResponseObserver);

        // Then
        verify(getUserResponseObserver).onNext(responseCaptor.capture());
        verify(getUserResponseObserver).onCompleted();

        GetUserResponse response = responseCaptor.getValue();
        assertThat(response.hasUser()).isFalse();
        assertThat(response.getErrorMessage()).contains("User not found: nonexistent");
    }

    @Test
    @DisplayName("getUser - 예외 발생")
    void getUser_Exception() {
        // Given
        GetUserRequest request = GetUserRequest.newBuilder()
                .setUserId("testuser")
                .build();

        when(userRepository.findByUsernameAndIsDeletedFalse("testuser"))
                .thenThrow(new RuntimeException("Database error"));

        ArgumentCaptor<GetUserResponse> responseCaptor = ArgumentCaptor.forClass(GetUserResponse.class);

        // When
        userGrpcService.getUser(request, getUserResponseObserver);

        // Then
        verify(getUserResponseObserver).onNext(responseCaptor.capture());
        verify(getUserResponseObserver).onCompleted();

        GetUserResponse response = responseCaptor.getValue();
        assertThat(response.hasUser()).isFalse();
        assertThat(response.getErrorMessage()).contains("Internal error: Database error");
    }

    @Test
    @DisplayName("getUsers - 배치 조회 성공")
    void getUsers_Success() {
        // Given
        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .passwordHash("$2a$12$encodedPassword")
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .isVerified(true)
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user2, "userId", 2L);
        ReflectionTestUtils.setField(user2, "createdAt", LocalDateTime.of(2024, 1, 2, 0, 0));
        ReflectionTestUtils.setField(user2, "updatedAt", LocalDateTime.of(2024, 1, 2, 0, 0));

        GetUsersRequest request = GetUsersRequest.newBuilder()
                .addAllUserIds(Arrays.asList("testuser", "user2"))
                .build();

        when(userRepository.findByUsernameInAndIsDeletedFalse(anyList()))
                .thenReturn(Arrays.asList(testUser, user2));

        ArgumentCaptor<GetUsersResponse> responseCaptor = ArgumentCaptor.forClass(GetUsersResponse.class);

        // When
        userGrpcService.getUsers(request, getUsersResponseObserver);

        // Then
        verify(getUsersResponseObserver).onNext(responseCaptor.capture());
        verify(getUsersResponseObserver).onCompleted();

        GetUsersResponse response = responseCaptor.getValue();
        assertThat(response.getUsersCount()).isEqualTo(2);
        assertThat(response.getUsersList())
                .extracting(com.hamkkebu.authservice.grpc.user.User::getUsername)
                .containsExactly("testuser", "user2");
        assertThat(response.getErrorMessage()).isEmpty();

        verify(userRepository).findByUsernameInAndIsDeletedFalse(Arrays.asList("testuser", "user2"));
    }

    @Test
    @DisplayName("getUsers - 일부만 존재")
    void getUsers_PartialResult() {
        // Given
        GetUsersRequest request = GetUsersRequest.newBuilder()
                .addAllUserIds(Arrays.asList("testuser", "nonexistent", "deleted"))
                .build();

        when(userRepository.findByUsernameInAndIsDeletedFalse(anyList()))
                .thenReturn(Arrays.asList(testUser)); // testuser만 조회됨

        ArgumentCaptor<GetUsersResponse> responseCaptor = ArgumentCaptor.forClass(GetUsersResponse.class);

        // When
        userGrpcService.getUsers(request, getUsersResponseObserver);

        // Then
        verify(getUsersResponseObserver).onNext(responseCaptor.capture());
        verify(getUsersResponseObserver).onCompleted();

        GetUsersResponse response = responseCaptor.getValue();
        assertThat(response.getUsersCount()).isEqualTo(1);
        assertThat(response.getUsers(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getUsers - 빈 결과")
    void getUsers_Empty() {
        // Given
        GetUsersRequest request = GetUsersRequest.newBuilder()
                .addAllUserIds(Arrays.asList("user1", "user2"))
                .build();

        when(userRepository.findByUsernameInAndIsDeletedFalse(anyList()))
                .thenReturn(Arrays.asList());

        ArgumentCaptor<GetUsersResponse> responseCaptor = ArgumentCaptor.forClass(GetUsersResponse.class);

        // When
        userGrpcService.getUsers(request, getUsersResponseObserver);

        // Then
        verify(getUsersResponseObserver).onNext(responseCaptor.capture());
        verify(getUsersResponseObserver).onCompleted();

        GetUsersResponse response = responseCaptor.getValue();
        assertThat(response.getUsersCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("getUsers - 예외 발생")
    void getUsers_Exception() {
        // Given
        GetUsersRequest request = GetUsersRequest.newBuilder()
                .addAllUserIds(Arrays.asList("testuser"))
                .build();

        when(userRepository.findByUsernameInAndIsDeletedFalse(anyList()))
                .thenThrow(new RuntimeException("Database error"));

        ArgumentCaptor<GetUsersResponse> responseCaptor = ArgumentCaptor.forClass(GetUsersResponse.class);

        // When
        userGrpcService.getUsers(request, getUsersResponseObserver);

        // Then
        verify(getUsersResponseObserver).onNext(responseCaptor.capture());
        verify(getUsersResponseObserver).onCompleted();

        GetUsersResponse response = responseCaptor.getValue();
        assertThat(response.getUsersCount()).isEqualTo(0);
        assertThat(response.getErrorMessage()).contains("Internal error: Database error");
    }

    @Test
    @DisplayName("userExists - 사용자 존재")
    void userExists_True() {
        // Given
        UserExistsRequest request = UserExistsRequest.newBuilder()
                .setUserId("testuser")
                .build();

        when(userRepository.existsByUsernameAndIsDeletedFalse("testuser"))
                .thenReturn(true);

        ArgumentCaptor<UserExistsResponse> responseCaptor = ArgumentCaptor.forClass(UserExistsResponse.class);

        // When
        userGrpcService.userExists(request, userExistsResponseObserver);

        // Then
        verify(userExistsResponseObserver).onNext(responseCaptor.capture());
        verify(userExistsResponseObserver).onCompleted();

        UserExistsResponse response = responseCaptor.getValue();
        assertThat(response.getExists()).isTrue();
        assertThat(response.getErrorMessage()).isEmpty();
    }

    @Test
    @DisplayName("userExists - 사용자 존재하지 않음")
    void userExists_False() {
        // Given
        UserExistsRequest request = UserExistsRequest.newBuilder()
                .setUserId("nonexistent")
                .build();

        when(userRepository.existsByUsernameAndIsDeletedFalse("nonexistent"))
                .thenReturn(false);

        ArgumentCaptor<UserExistsResponse> responseCaptor = ArgumentCaptor.forClass(UserExistsResponse.class);

        // When
        userGrpcService.userExists(request, userExistsResponseObserver);

        // Then
        verify(userExistsResponseObserver).onNext(responseCaptor.capture());
        verify(userExistsResponseObserver).onCompleted();

        UserExistsResponse response = responseCaptor.getValue();
        assertThat(response.getExists()).isFalse();
        assertThat(response.getErrorMessage()).isEmpty();
    }

    @Test
    @DisplayName("userExists - 예외 발생")
    void userExists_Exception() {
        // Given
        UserExistsRequest request = UserExistsRequest.newBuilder()
                .setUserId("testuser")
                .build();

        when(userRepository.existsByUsernameAndIsDeletedFalse("testuser"))
                .thenThrow(new RuntimeException("Database error"));

        ArgumentCaptor<UserExistsResponse> responseCaptor = ArgumentCaptor.forClass(UserExistsResponse.class);

        // When
        userGrpcService.userExists(request, userExistsResponseObserver);

        // Then
        verify(userExistsResponseObserver).onNext(responseCaptor.capture());
        verify(userExistsResponseObserver).onCompleted();

        UserExistsResponse response = responseCaptor.getValue();
        assertThat(response.getExists()).isFalse();
        assertThat(response.getErrorMessage()).contains("Internal error: Database error");
    }
}
