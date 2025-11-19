package com.hamkkebu.boilerplate.controller;

import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import com.hamkkebu.boilerplate.data.dto.EventPublishResponse;
import com.hamkkebu.boilerplate.data.dto.PublishLedgerEventRequest;
import com.hamkkebu.boilerplate.data.dto.PublishTransactionEventRequest;
import com.hamkkebu.boilerplate.data.dto.PublishUserEventRequest;
import com.hamkkebu.boilerplate.data.event.*;
import com.hamkkebu.boilerplate.publisher.OutboxEventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 이벤트 발행 예제 컨트롤러
 *
 * <p>이 컨트롤러는 Zero-Payload Kafka 이벤트 발행 예제를 제공합니다.</p>
 * <p>실제 프로덕션에서는 비즈니스 로직 내에서 이벤트를 발행해야 합니다.</p>
 *
 * <p>RBAC SECURITY: 이 API는 DEVELOPER 권한이 있는 사용자만 접근 가능합니다.</p>
 * <p>개발/테스트용 API이므로 일반 사용자의 접근을 제한합니다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/events/examples")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_DEVELOPER')")
@Tag(name = "Event Examples", description = "이벤트 발행 예제 API (DEVELOPER 전용)")
public class EventExampleController {

    private final OutboxEventPublisher outboxEventPublisher;

    /**
     * 사용자 생성 이벤트 발행 예제 (Transactional Outbox 패턴)
     */
    @Operation(summary = "사용자 생성 이벤트 발행", description = "UserCreatedEvent를 Outbox 테이블에 저장합니다 (개발/테스트용)")
    @PostMapping("/user-created")
    @Transactional
    public ApiResponse<EventPublishResponse> publishUserCreatedEvent(
        @Valid @RequestBody PublishUserEventRequest request
    ) {
        log.info("Publishing UserCreatedEvent for userId={}", request.getUserId());

        // 이벤트 생성
        UserCreatedEvent event = UserCreatedEvent.builder()
            .userId(request.getUserId())
            .metadata(request.getMetadata() != null ? request.getMetadata() : "{\"source\": \"api\"}")
            .build();

        // 이벤트 발행 (Outbox 패턴 - DB에 먼저 저장)
        outboxEventPublisher.publish("user.events", event);

        EventPublishResponse response = new EventPublishResponse(
            event.getEventId(),
            request.getUserId(),
            "user.events"
        );

        return ApiResponse.success(response, "UserCreatedEvent가 Outbox에 저장되었습니다 (Scheduler가 Kafka로 발행 예정)");
    }

    /**
     * 가계부 생성 이벤트 발행 예제 (Transactional Outbox 패턴)
     */
    @Operation(summary = "가계부 생성 이벤트 발행", description = "LedgerCreatedEvent를 Outbox 테이블에 저장합니다 (개발/테스트용)")
    @PostMapping("/ledger-created")
    @Transactional
    public ApiResponse<EventPublishResponse> publishLedgerCreatedEvent(
        @Valid @RequestBody PublishLedgerEventRequest request
    ) {
        log.info("Publishing LedgerCreatedEvent for ledgerId={}, userId={}", request.getLedgerId(), request.getUserId());

        // 이벤트 생성
        LedgerCreatedEvent event = new LedgerCreatedEvent(request.getLedgerId(), request.getUserId());

        // 이벤트 발행 (Outbox 패턴)
        outboxEventPublisher.publish("ledger.events", event);

        EventPublishResponse response = new EventPublishResponse(
            event.getEventId(),
            request.getLedgerId(),
            "ledger.events"
        );

        return ApiResponse.success(response, "LedgerCreatedEvent가 Outbox에 저장되었습니다");
    }

    /**
     * 거래 생성 이벤트 발행 예제 (Transactional Outbox 패턴)
     */
    @Operation(summary = "거래 생성 이벤트 발행", description = "TransactionCreatedEvent를 Outbox 테이블에 저장합니다 (개발/테스트용)")
    @PostMapping("/transaction-created")
    @Transactional
    public ApiResponse<EventPublishResponse> publishTransactionCreatedEvent(
        @Valid @RequestBody PublishTransactionEventRequest request
    ) {
        log.info("Publishing TransactionCreatedEvent for transactionId={}, userId={}, ledgerId={}",
            request.getTransactionId(), request.getUserId(), request.getLedgerId());

        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
            .transactionId(request.getTransactionId())
            .userId(request.getUserId())
            .ledgerId(request.getLedgerId())
            .build();

        // Outbox 패턴으로 발행
        outboxEventPublisher.publish("transaction.events", event);

        EventPublishResponse response = new EventPublishResponse(
            event.getEventId(),
            request.getTransactionId(),
            "transaction.events"
        );

        return ApiResponse.success(response, "TransactionCreatedEvent가 Outbox에 저장되었습니다");
    }

    /**
     * 거래 수정 이벤트 발행 예제 (Transactional Outbox 패턴)
     */
    @Operation(summary = "거래 수정 이벤트 발행", description = "TransactionUpdatedEvent를 Outbox 테이블에 저장합니다 (개발/테스트용)")
    @PostMapping("/transaction-updated")
    @Transactional
    public ApiResponse<EventPublishResponse> publishTransactionUpdatedEvent(
        @Valid @RequestBody PublishTransactionEventRequest request
    ) {
        log.info("Publishing TransactionUpdatedEvent for transactionId={}", request.getTransactionId());

        TransactionUpdatedEvent event = new TransactionUpdatedEvent(
            request.getTransactionId(),
            request.getUserId(),
            request.getLedgerId()
        );
        outboxEventPublisher.publish("transaction.events", event);

        EventPublishResponse response = new EventPublishResponse(
            event.getEventId(),
            request.getTransactionId(),
            "transaction.events"
        );

        return ApiResponse.success(response, "TransactionUpdatedEvent가 Outbox에 저장되었습니다");
    }

    /**
     * 거래 삭제 이벤트 발행 예제 (Transactional Outbox 패턴)
     */
    @Operation(summary = "거래 삭제 이벤트 발행", description = "TransactionDeletedEvent를 Outbox 테이블에 저장합니다 (개발/테스트용)")
    @PostMapping("/transaction-deleted")
    @Transactional
    public ApiResponse<EventPublishResponse> publishTransactionDeletedEvent(
        @Valid @RequestBody PublishTransactionEventRequest request
    ) {
        log.info("Publishing TransactionDeletedEvent for transactionId={}", request.getTransactionId());

        TransactionDeletedEvent event = new TransactionDeletedEvent(
            request.getTransactionId(),
            request.getUserId(),
            request.getLedgerId()
        );
        outboxEventPublisher.publish("transaction.events", event);

        EventPublishResponse response = new EventPublishResponse(
            event.getEventId(),
            request.getTransactionId(),
            "transaction.events"
        );

        return ApiResponse.success(response, "TransactionDeletedEvent가 Outbox에 저장되었습니다");
    }
}
