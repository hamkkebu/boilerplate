package com.hamkkebu.boilerplate.data.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Zero-Payload 이벤트 기본 클래스
 *
 * <p>이 클래스는 이벤트에 전체 데이터를 포함하지 않고,
 * 필요한 최소한의 정보만 포함합니다 (Zero-Payload 패턴)</p>
 *
 * <p>장점:</p>
 * <ul>
 *   <li>이벤트 크기 최소화 → 네트워크 효율성</li>
 *   <li>데이터 일관성 보장 (항상 최신 데이터를 조회)</li>
 *   <li>보안 강화 (민감한 정보 노출 방지)</li>
 *   <li>버전 관리 용이 (데이터 구조 변경에 유연)</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public abstract class BaseEvent implements DomainEvent {

    /**
     * 이벤트 고유 ID (자동 생성)
     */
    private String eventId = UUID.randomUUID().toString();

    /**
     * 이벤트 타입 (예: USER_CREATED, TRANSACTION_CREATED)
     */
    private String eventType;

    /**
     * 이벤트 스키마 버전
     *
     * <p>이벤트 스키마 진화(evolution)를 위한 버전 정보</p>
     * <p>형식: "major.minor" (예: "1.0", "1.1", "2.0")</p>
     *
     * <p>버전 정책:</p>
     * <ul>
     *   <li>Major: 호환되지 않는 변경 (필드 제거, 타입 변경)</li>
     *   <li>Minor: 하위 호환 가능한 변경 (필드 추가)</li>
     * </ul>
     */
    private String eventVersion = "1.0";

    /**
     * 리소스 ID (주요 엔티티의 ID)
     * 예: 사용자 ID, 거래 ID, 가계부 ID 등
     */
    private String resourceId;

    /**
     * 이벤트 발생 시각 (자동 설정)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime occurredAt = LocalDateTime.now();

    /**
     * 이벤트를 발생시킨 사용자 ID (선택적)
     */
    private String userId;

    /**
     * 추가 메타데이터 (JSON 형식, 선택적)
     */
    private String metadata;

    /**
     * Builder 패턴을 위한 생성자
     */
    protected BaseEvent(String eventType, String resourceId, String userId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.eventVersion = "1.0";  // 기본 버전
        this.resourceId = resourceId;
        this.occurredAt = LocalDateTime.now();
        this.userId = userId;
    }

    /**
     * Builder 패턴을 위한 생성자 (metadata 포함)
     */
    protected BaseEvent(String eventType, String resourceId, String userId, String metadata) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.eventVersion = "1.0";  // 기본 버전
        this.resourceId = resourceId;
        this.occurredAt = LocalDateTime.now();
        this.userId = userId;
        this.metadata = metadata;
    }
}
