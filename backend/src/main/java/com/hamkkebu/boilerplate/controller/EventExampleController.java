package com.hamkkebu.boilerplate.controller;

import com.hamkkebu.boilerplate.data.event.*;
import com.hamkkebu.boilerplate.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 이벤트 발행 예제 컨트롤러
 *
 * <p>이 컨트롤러는 Zero-Payload Kafka 이벤트 발행 예제를 제공합니다.</p>
 * <p>실제 프로덕션에서는 비즈니스 로직 내에서 이벤트를 발행해야 합니다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/events/examples")
@RequiredArgsConstructor
public class EventExampleController {

    private final EventPublisher eventPublisher;

    /**
     * 사용자 생성 이벤트 발행 예제
     *
     * POST /api/v1/events/examples/user-created?userId=user-123
     */
    @PostMapping("/user-created")
    public ResponseEntity<Map<String, String>> publishUserCreatedEvent(
        @RequestParam String userId
    ) {
        log.info("Publishing UserCreatedEvent for userId={}", userId);

        // 방법 1: Builder 패턴
        UserCreatedEvent event = UserCreatedEvent.builder()
            .userId(userId)
            .metadata("{\"source\": \"web\", \"ipAddress\": \"127.0.0.1\"}")
            .build();

        // 이벤트 발행 (비동기)
        eventPublisher.publish("user.events", event);

        return ResponseEntity.ok(Map.of(
            "message", "UserCreatedEvent published successfully",
            "eventId", event.getEventId(),
            "userId", userId
        ));
    }

    /**
     * 가계부 생성 이벤트 발행 예제
     *
     * POST /api/v1/events/examples/ledger-created?ledgerId=ledger-123&userId=user-123
     */
    @PostMapping("/ledger-created")
    public ResponseEntity<Map<String, String>> publishLedgerCreatedEvent(
        @RequestParam String ledgerId,
        @RequestParam String userId
    ) {
        log.info("Publishing LedgerCreatedEvent for ledgerId={}, userId={}", ledgerId, userId);

        // 방법 2: 간단한 생성자
        LedgerCreatedEvent event = new LedgerCreatedEvent(ledgerId, userId);

        // 이벤트 발행
        eventPublisher.publish("ledger.events", event);

        return ResponseEntity.ok(Map.of(
            "message", "LedgerCreatedEvent published successfully",
            "eventId", event.getEventId(),
            "ledgerId", ledgerId,
            "userId", userId
        ));
    }

    /**
     * 거래 생성 이벤트 발행 예제
     *
     * POST /api/v1/events/examples/transaction-created
     * Body: {"transactionId": "tx-123", "userId": "user-123", "ledgerId": "ledger-123"}
     */
    @PostMapping("/transaction-created")
    public ResponseEntity<Map<String, String>> publishTransactionCreatedEvent(
        @RequestBody Map<String, String> request
    ) {
        String transactionId = request.get("transactionId");
        String userId = request.get("userId");
        String ledgerId = request.get("ledgerId");

        log.info("Publishing TransactionCreatedEvent for transactionId={}, userId={}, ledgerId={}",
            transactionId, userId, ledgerId);

        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
            .transactionId(transactionId)
            .userId(userId)
            .ledgerId(ledgerId)
            .build();

        // 동기 발행 예제 (발행 완료를 기다림)
        eventPublisher.publishSync("transaction.events", event);

        return ResponseEntity.ok(Map.of(
            "message", "TransactionCreatedEvent published successfully (sync)",
            "eventId", event.getEventId(),
            "transactionId", transactionId
        ));
    }

    /**
     * 거래 수정 이벤트 발행 예제
     */
    @PostMapping("/transaction-updated")
    public ResponseEntity<Map<String, String>> publishTransactionUpdatedEvent(
        @RequestBody Map<String, String> request
    ) {
        String transactionId = request.get("transactionId");
        String userId = request.get("userId");
        String ledgerId = request.get("ledgerId");

        log.info("Publishing TransactionUpdatedEvent for transactionId={}", transactionId);

        TransactionUpdatedEvent event = new TransactionUpdatedEvent(transactionId, userId, ledgerId);
        eventPublisher.publish("transaction.events", event);

        return ResponseEntity.ok(Map.of(
            "message", "TransactionUpdatedEvent published successfully",
            "eventId", event.getEventId()
        ));
    }

    /**
     * 거래 삭제 이벤트 발행 예제
     */
    @PostMapping("/transaction-deleted")
    public ResponseEntity<Map<String, String>> publishTransactionDeletedEvent(
        @RequestBody Map<String, String> request
    ) {
        String transactionId = request.get("transactionId");
        String userId = request.get("userId");
        String ledgerId = request.get("ledgerId");

        log.info("Publishing TransactionDeletedEvent for transactionId={}", transactionId);

        TransactionDeletedEvent event = new TransactionDeletedEvent(transactionId, userId, ledgerId);
        eventPublisher.publish("transaction.events", event);

        return ResponseEntity.ok(Map.of(
            "message", "TransactionDeletedEvent published successfully",
            "eventId", event.getEventId()
        ));
    }
}
