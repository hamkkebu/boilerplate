package com.hamkkebu.boilerplate.common.user.consumer;

import com.hamkkebu.boilerplate.common.user.entity.SyncedUser;
import com.hamkkebu.boilerplate.common.user.event.UserDeletedEvent;
import com.hamkkebu.boilerplate.common.user.event.UserRegisteredEvent;
import com.hamkkebu.boilerplate.common.user.repository.SyncedUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * 사용자 이벤트 Kafka Consumer 추상 클래스
 *
 * <p>auth-service에서 발행한 사용자 관련 이벤트를 수신합니다.</p>
 * <ul>
 *   <li>USER_REGISTERED: 신규 사용자 동기화</li>
 *   <li>USER_DELETED: 사용자 삭제 (soft delete)</li>
 * </ul>
 * <p>Zero-Payload 패턴에 따라 이벤트 수신 후 gRPC로 상세 정보를 조회합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Slf4j
 * @Component
 * public class LedgerUserEventConsumer extends AbstractUserEventConsumer<LedgerUser> {
 *
 *     private final AuthServiceGrpcClient authServiceGrpcClient;
 *
 *     public LedgerUserEventConsumer(LedgerUserRepository userRepository,
 *                                     AuthServiceGrpcClient grpcClient) {
 *         super(userRepository);
 *         this.authServiceGrpcClient = grpcClient;
 *     }
 *
 *     @Override
 *     protected Optional<LedgerUser> fetchAndCreateUser(Long userId) {
 *         return authServiceGrpcClient.getUser(userId)
 *             .map(grpcUser -> LedgerUser.builder()
 *                 .userId(grpcUser.getUserId())
 *                 .username(grpcUser.getUsername())
 *                 .email(grpcUser.getEmail())
 *                 .firstName(grpcUser.getFirstName().isEmpty() ? null : grpcUser.getFirstName())
 *                 .lastName(grpcUser.getLastName().isEmpty() ? null : grpcUser.getLastName())
 *                 .isActive(grpcUser.getIsActive())
 *                 .role(Role.fromString(grpcUser.getRole()))
 *                 .build());
 *     }
 *
 *     @KafkaListener(
 *             topics = "${kafka.topics.user-events:user.events}",
 *             groupId = "ledger-service-group",
 *             containerFactory = "ledgerKafkaListenerContainerFactory"
 *     )
 *     @Transactional
 *     public void handleUserEvent(Map<String, Object> eventData) {
 *         processUserEvent(eventData);
 *     }
 * }
 * }
 * </pre>
 *
 * @param <T> SyncedUser를 상속받은 엔티티 타입
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractUserEventConsumer<T extends SyncedUser> {

    private final SyncedUserRepository<T> userRepository;

    /**
     * 사용자 이벤트 처리 (서브클래스에서 KafkaListener와 함께 호출)
     *
     * @param eventData 이벤트 데이터 맵
     */
    @Transactional
    protected void processUserEvent(Map<String, Object> eventData) {
        String eventType = (String) eventData.get("eventType");
        String eventId = (String) eventData.get("eventId");

        log.info("[Kafka Consumer] Received event: eventType={}, eventId={}", eventType, eventId);

        try {
            if (UserRegisteredEvent.EVENT_TYPE.equals(eventType)) {
                handleUserRegistered(eventData);
            } else if (UserDeletedEvent.EVENT_TYPE.equals(eventType)) {
                handleUserDeleted(eventData);
            } else {
                log.warn("[Kafka Consumer] Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("[Kafka Consumer] Failed to process event: eventType={}, eventId={}, error={}",
                    eventType, eventId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * USER_REGISTERED 이벤트 처리
     *
     * <p>신규 사용자가 회원가입하면:</p>
     * <ol>
     *   <li>Kafka 이벤트로 userId를 수신</li>
     *   <li>gRPC로 auth-service에서 사용자 상세 정보 조회</li>
     *   <li>서비스 DB에 사용자 정보 저장</li>
     * </ol>
     */
    private void handleUserRegistered(Map<String, Object> eventData) {
        Long userId = extractUserPk(eventData);
        log.info("[Kafka Consumer] Processing USER_REGISTERED: userId={}", userId);

        // 이미 존재하는 사용자인지 확인
        if (userRepository.existsByUserIdAndIsDeletedFalse(userId)) {
            log.info("[Kafka Consumer] User already exists: userId={}", userId);
            return;
        }

        // gRPC로 auth-service에서 사용자 정보 조회 및 엔티티 생성
        fetchAndCreateUser(userId).ifPresentOrElse(
                user -> {
                    userRepository.save(user);
                    log.info("[Kafka Consumer] User synced successfully: userId={}, username={}",
                            user.getUserId(), user.getUsername());
                },
                () -> log.warn("[Kafka Consumer] User not found in auth-service: userId={}", userId)
        );
    }

    /**
     * USER_DELETED 이벤트 처리
     *
     * <p>사용자가 탈퇴하면:</p>
     * <ol>
     *   <li>Kafka 이벤트로 userId를 수신</li>
     *   <li>서비스 DB에서 해당 사용자 soft delete</li>
     * </ol>
     */
    private void handleUserDeleted(Map<String, Object> eventData) {
        Long userId = extractUserPk(eventData);
        log.info("[Kafka Consumer] Processing USER_DELETED: userId={}", userId);

        // 사용자 조회 및 삭제 처리
        userRepository.findByUserIdAndIsDeletedFalse(userId).ifPresentOrElse(
                user -> {
                    user.delete();
                    userRepository.save(user);
                    log.info("[Kafka Consumer] User deleted successfully: userId={}, username={}",
                            user.getUserId(), user.getUsername());
                },
                () -> log.warn("[Kafka Consumer] User not found for deletion: userId={}", userId)
        );
    }

    /**
     * 이벤트 데이터에서 userPk 추출
     */
    protected Long extractUserPk(Map<String, Object> eventData) {
        Object userPk = eventData.get("userPk");
        if (userPk instanceof Number) {
            return ((Number) userPk).longValue();
        }
        if (userPk instanceof String) {
            return Long.parseLong((String) userPk);
        }
        throw new IllegalArgumentException("Invalid userPk format: " + userPk);
    }

    /**
     * gRPC로 auth-service에서 사용자 정보 조회 및 엔티티 생성 (서비스별 구현 필요)
     *
     * <p>각 서비스에서는 자체 gRPC 클라이언트를 사용하여 사용자 정보를 조회하고
     * 해당 서비스의 User 엔티티로 변환하여 반환해야 합니다.</p>
     *
     * @param userId 사용자 ID
     * @return 생성된 User 엔티티 Optional (사용자가 없으면 empty)
     */
    protected abstract Optional<T> fetchAndCreateUser(Long userId);
}
