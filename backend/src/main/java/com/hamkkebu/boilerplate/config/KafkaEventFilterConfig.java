package com.hamkkebu.boilerplate.config;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

/**
 * Kafka 이벤트 필터 설정
 *
 * <p>특정 이벤트 타입만 처리하도록 필터링합니다.</p>
 */
@Configuration
public class KafkaEventFilterConfig {

    /**
     * USER_CREATED 이벤트만 필터링
     */
    @Bean
    public RecordFilterStrategy<String, Object> userCreatedEventFilter() {
        return record -> {
            if (record.value() instanceof BaseEvent event) {
                return !"USER_CREATED".equals(event.getEventType());
            }
            return true; // BaseEvent가 아니면 필터링
        };
    }

    /**
     * TRANSACTION_CREATED 이벤트만 필터링
     */
    @Bean
    public RecordFilterStrategy<String, Object> transactionCreatedEventFilter() {
        return record -> {
            if (record.value() instanceof BaseEvent event) {
                return !"TRANSACTION_CREATED".equals(event.getEventType());
            }
            return true;
        };
    }

    /**
     * TRANSACTION_UPDATED 이벤트만 필터링
     */
    @Bean
    public RecordFilterStrategy<String, Object> transactionUpdatedEventFilter() {
        return record -> {
            if (record.value() instanceof BaseEvent event) {
                return !"TRANSACTION_UPDATED".equals(event.getEventType());
            }
            return true;
        };
    }

    /**
     * TRANSACTION_DELETED 이벤트만 필터링
     */
    @Bean
    public RecordFilterStrategy<String, Object> transactionDeletedEventFilter() {
        return record -> {
            if (record.value() instanceof BaseEvent event) {
                return !"TRANSACTION_DELETED".equals(event.getEventType());
            }
            return true;
        };
    }
}
