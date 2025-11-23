package com.hamkkebu.boilerplate.data.event;

import java.time.LocalDateTime;

/**
 * 도메인 이벤트 인터페이스
 * Zero-Payload 패턴을 따르는 모든 이벤트가 구현해야 하는 인터페이스
 */
public interface DomainEvent {

    /**
     * 이벤트 고유 ID
     */
    String getEventId();

    /**
     * 이벤트 타입 (예: USER_CREATED, TRANSACTION_CREATED)
     */
    String getEventType();

    /**
     * 리소스 ID (주요 엔티티의 ID)
     */
    String getResourceId();

    /**
     * 이벤트 발생 시각
     */
    LocalDateTime getOccurredAt();

    /**
     * 이벤트를 발생시킨 사용자 ID (선택적)
     */
    String getUserId();

    /**
     * 추가 메타데이터 (선택적)
     */
    default String getMetadata() {
        return null;
    }
}
