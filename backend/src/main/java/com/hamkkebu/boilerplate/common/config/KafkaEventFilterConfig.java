package com.hamkkebu.boilerplate.common.config;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

/**
 * Kafka 이벤트 필터 설정
 *
 * <p>특정 이벤트 타입만 처리하도록 필터링합니다.</p>
 *
 * <p>필터는 리스너가 특정 이벤트 타입만 수신하도록 합니다.
 * 예를 들어, userCreatedEventFilter는 USER_CREATED 이벤트만 통과시키고 나머지는 필터링합니다.</p>
 */
@Configuration
public class KafkaEventFilterConfig {

    /**
     * 특정 이벤트 타입만 통과시키는 필터 생성
     *
     * @param eventType 통과시킬 이벤트 타입 (예: "USER_CREATED")
     * @return RecordFilterStrategy - true를 반환하면 필터링(무시), false를 반환하면 통과
     */
    private RecordFilterStrategy<String, Object> createEventTypeFilter(String eventType) {
        return record -> {
            if (record.value() instanceof BaseEvent event) {
                // 해당 이벤트 타입이 아니면 필터링(true 반환)
                return !eventType.equals(event.getEventType());
            }
            // BaseEvent가 아니면 필터링
            return true;
        };
    }

    /**
     * USER_CREATED 이벤트만 필터링
     */
    @Bean
    public RecordFilterStrategy<String, Object> userCreatedEventFilter() {
        return createEventTypeFilter("USER_CREATED");
    }

    /**
     * TRANSACTION_CREATED 이벤트만 필터링
     */
    @Bean
    public RecordFilterStrategy<String, Object> transactionCreatedEventFilter() {
        return createEventTypeFilter("TRANSACTION_CREATED");
    }

    /**
     * TRANSACTION_UPDATED 이벤트만 필터링
     */
    @Bean
    public RecordFilterStrategy<String, Object> transactionUpdatedEventFilter() {
        return createEventTypeFilter("TRANSACTION_UPDATED");
    }

    /**
     * TRANSACTION_DELETED 이벤트만 필터링
     */
    @Bean
    public RecordFilterStrategy<String, Object> transactionDeletedEventFilter() {
        return createEventTypeFilter("TRANSACTION_DELETED");
    }
}
