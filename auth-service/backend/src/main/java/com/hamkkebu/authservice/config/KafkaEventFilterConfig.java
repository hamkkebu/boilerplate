package com.hamkkebu.authservice.config;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

/**
 * Kafka 이벤트 필터 설정
 *
 * <p>Auth Service는 USER 관련 이벤트만 처리합니다.</p>
 */
@Configuration
public class KafkaEventFilterConfig {

    /**
     * USER_CREATED 이벤트만 필터링
     *
     * @return USER_CREATED 이벤트만 통과시키는 필터
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
}
