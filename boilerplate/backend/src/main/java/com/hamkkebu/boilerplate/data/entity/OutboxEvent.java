package com.hamkkebu.boilerplate.data.entity;

import com.hamkkebu.boilerplate.common.enums.OutboxEventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Outbox Event 엔티티
 *
 * <p>Transactional Outbox 패턴을 구현하기 위한 엔티티</p>
 *
 * <p>동작 방식:</p>
 * <ol>
 *   <li>비즈니스 로직 트랜잭션 내에서 이벤트를 Outbox 테이블에 저장</li>
 *   <li>트랜잭션 커밋되면 이벤트도 함께 저장됨 (원자성 보장)</li>
 *   <li>별도의 Scheduler가 PENDING 상태의 이벤트를 주기적으로 조회</li>
 *   <li>Kafka로 발행 성공 시 PUBLISHED 상태로 변경</li>
 *   <li>발행 실패 시 재시도, 최대 재시도 횟수 초과 시 FAILED 상태로 변경</li>
 * </ol>
 *
 * <p>장점:</p>
 * <ul>
 *   <li>DB 트랜잭션과 이벤트 발행의 원자성 보장</li>
 *   <li>이벤트 발행 실패 시에도 데이터 손실 방지</li>
 *   <li>재시도 메커니즘으로 일시적 장애 대응</li>
 *   <li>발행 실패한 이벤트 추적 및 모니터링 가능</li>
 * </ul>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_outbox_event")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 이벤트 고유 ID (UUID)
     */
    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    /**
     * 이벤트 타입 (예: USER_CREATED, TRANSACTION_CREATED)
     */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    /**
     * Kafka 토픽명
     */
    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    /**
     * 리소스 ID (Kafka 파티션 키로 사용)
     */
    @Column(name = "resource_id", nullable = false, length = 100)
    private String resourceId;

    /**
     * 이벤트 전체 Payload (JSON 형태)
     */
    @Column(name = "payload", nullable = false, columnDefinition = "JSON")
    private String payload;

    /**
     * 이벤트 상태 (PENDING, PUBLISHED, FAILED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OutboxEventStatus status = OutboxEventStatus.PENDING;

    /**
     * 재시도 횟수
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 최대 재시도 횟수
     */
    @Column(name = "max_retry", nullable = false)
    @Builder.Default
    private Integer maxRetry = 3;

    /**
     * 에러 메시지 (발행 실패 시)
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 발행 완료 시각
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 마지막 재시도 시각
     */
    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    /**
     * 발행 성공 처리
     */
    public void markAsPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * 발행 실패 처리
     */
    public void markAsFailed(String errorMessage) {
        this.status = OutboxEventStatus.FAILED;
        this.errorMessage = errorMessage;
        this.lastRetryAt = LocalDateTime.now();
    }

    /**
     * 재시도 증가
     */
    public void incrementRetry() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }

    /**
     * 재시도 가능 여부 확인
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetry;
    }
}
