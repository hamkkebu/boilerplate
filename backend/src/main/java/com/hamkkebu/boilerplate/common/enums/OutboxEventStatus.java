package com.hamkkebu.boilerplate.common.enums;

/**
 * Outbox 이벤트 상태 Enum
 *
 * <p>Transactional Outbox 패턴에서 이벤트의 처리 상태를 나타냅니다.</p>
 *
 * <ul>
 *   <li>PENDING: 발행 대기 중 (DB에 저장되었지만 아직 Kafka로 발행되지 않음)</li>
 *   <li>PUBLISHED: 발행 완료 (Kafka로 성공적으로 발행됨)</li>
 *   <li>FAILED: 발행 실패 (여러 번 재시도했지만 실패)</li>
 * </ul>
 */
public enum OutboxEventStatus {
    /**
     * 발행 대기 중
     * - DB에 저장되었지만 아직 Kafka로 발행되지 않음
     * - Scheduler가 이 상태의 이벤트를 찾아서 발행
     */
    PENDING,

    /**
     * 발행 완료
     * - Kafka로 성공적으로 발행됨
     * - 이 상태의 이벤트는 일정 시간 후 삭제 가능
     */
    PUBLISHED,

    /**
     * 발행 실패
     * - 여러 번 재시도했지만 실패
     * - 수동으로 확인 및 처리 필요
     */
    FAILED
}
