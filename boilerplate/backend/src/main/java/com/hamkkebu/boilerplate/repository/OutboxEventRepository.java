package com.hamkkebu.boilerplate.repository;

import com.hamkkebu.boilerplate.common.enums.OutboxEventStatus;
import com.hamkkebu.boilerplate.data.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox Event Repository
 *
 * <p>Transactional Outbox 패턴의 이벤트를 관리하는 리포지토리</p>
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * PENDING 상태의 이벤트 조회 (발행 대기 중)
     *
     * <p>생성 시간 순으로 정렬하여 FIFO 방식으로 처리</p>
     *
     * @return PENDING 상태의 이벤트 목록
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    /**
     * 특정 시간 이전에 PUBLISHED된 이벤트 삭제
     *
     * <p>PUBLISHED 상태의 오래된 이벤트를 정리하여 테이블 크기 관리</p>
     *
     * @param status 상태 (PUBLISHED)
     * @param before 기준 시간 (이 시간 이전에 발행된 이벤트 삭제)
     * @return 삭제된 이벤트 개수
     */
    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.status = :status AND o.publishedAt < :before")
    int deleteByStatusAndPublishedAtBefore(
        @Param("status") OutboxEventStatus status,
        @Param("before") LocalDateTime before
    );

    /**
     * 이벤트 ID로 조회
     *
     * @param eventId 이벤트 ID
     * @return OutboxEvent
     */
    OutboxEvent findByEventId(String eventId);

    /**
     * 특정 상태의 이벤트 개수 조회
     *
     * @param status 상태
     * @return 이벤트 개수
     */
    long countByStatus(OutboxEventStatus status);
}
