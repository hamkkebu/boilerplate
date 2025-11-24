package com.hamkkebu.authservice.grpc.server;

import com.hamkkebu.authservice.data.entity.User;
import com.hamkkebu.authservice.grpc.user.*;
import com.hamkkebu.authservice.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 gRPC 서비스 (내부 API)
 *
 * <p>Zero-Payload 패턴에서 다른 마이크로서비스가 사용자 정보를 조회할 때 사용</p>
 *
 * <p>아키텍처:</p>
 * <pre>
 * [외부 API] REST Controller → UserRepository
 *                                   ↓
 * [내부 API] gRPC Service   → UserRepository (공유)
 *                                   ↓
 *                              Database
 * </pre>
 *
 * <p>Port: 9090 (기본값, application.yml에서 변경 가능)</p>
 *
 * <p>예시 호출:</p>
 * <pre>
 * // 다른 서비스의 EventListener에서:
 * UserServiceGrpc.UserServiceBlockingStub stub = ...;
 * GetUserRequest request = GetUserRequest.newBuilder()
 *     .setUserId(event.getUserId())
 *     .build();
 * GetUserResponse response = stub.getUser(request);
 * com.hamkkebu.authservice.grpc.user.User user = response.getUser();
 * </pre>
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 사용자 ID로 사용자 정보 조회
     */
    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        log.info("[gRPC] GetUser request: userId={}", request.getUserId());

        try {
            // Repository 직접 조회 (username으로 조회)
            Optional<User> userOpt = userRepository.findByUsernameAndIsDeletedFalse(request.getUserId());

            if (userOpt.isEmpty()) {
                GetUserResponse response = GetUserResponse.newBuilder()
                    .setErrorMessage("User not found: " + request.getUserId())
                    .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            User user = userOpt.get();
            com.hamkkebu.authservice.grpc.user.User protoUser = convertToProtoUser(user);

            GetUserResponse response = GetUserResponse.newBuilder()
                .setUser(protoUser)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("[gRPC] GetUser success: userId={}", request.getUserId());

        } catch (Exception e) {
            log.error("[gRPC] GetUser failed: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            GetUserResponse response = GetUserResponse.newBuilder()
                .setErrorMessage("Internal error: " + e.getMessage())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * 여러 사용자 ID로 배치 조회
     */
    @Override
    public void getUsers(GetUsersRequest request, StreamObserver<GetUsersResponse> responseObserver) {
        log.info("[gRPC] GetUsers request: count={}", request.getUserIdsCount());

        try {
            List<String> usernames = request.getUserIdsList();

            // PERFORMANCE: N+1 쿼리 최적화 - 단일 IN 쿼리로 일괄 조회
            List<User> users = userRepository.findByUsernameInAndIsDeletedFalse(usernames);

            List<com.hamkkebu.authservice.grpc.user.User> protoUsers = users.stream()
                .map(this::convertToProtoUser)
                .toList();

            GetUsersResponse response = GetUsersResponse.newBuilder()
                .addAllUsers(protoUsers)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("[gRPC] GetUsers success: found={}/{}", users.size(), usernames.size());

        } catch (Exception e) {
            log.error("[gRPC] GetUsers failed: error={}", e.getMessage(), e);
            GetUsersResponse response = GetUsersResponse.newBuilder()
                .setErrorMessage("Internal error: " + e.getMessage())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * 사용자 존재 여부 확인
     */
    @Override
    public void userExists(UserExistsRequest request, StreamObserver<UserExistsResponse> responseObserver) {
        log.debug("[gRPC] UserExists request: userId={}", request.getUserId());

        try {
            // Repository로 존재 여부 확인 (Soft Delete 체크 포함)
            boolean exists = userRepository.existsByUsernameAndIsDeletedFalse(request.getUserId());

            UserExistsResponse response = UserExistsResponse.newBuilder()
                .setExists(exists)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("[gRPC] UserExists failed: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            UserExistsResponse response = UserExistsResponse.newBuilder()
                .setExists(false)
                .setErrorMessage("Internal error: " + e.getMessage())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * Entity를 Proto User로 변환
     */
    private com.hamkkebu.authservice.grpc.user.User convertToProtoUser(User user) {
        return com.hamkkebu.authservice.grpc.user.User.newBuilder()
            .setId(String.valueOf(user.getUserId()))  // PK
            .setUsername(user.getUsername())
            .setEmail(user.getEmail() != null ? user.getEmail() : "")
            .setNickname("")  // User entity doesn't have nickname field
            .setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().format(FORMATTER) : "")
            .setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().format(FORMATTER) : "")
            .setStatus(user.getIsActive() ? UserStatus.ACTIVE : UserStatus.INACTIVE)
            .build();
    }
}
