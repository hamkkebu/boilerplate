package com.hamkkebu.boilerplate.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 설정 클래스
 * Producer와 Consumer 설정을 관리합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>DLQ (Dead Letter Queue) 설정</li>
 *   <li>재시도 정책 (Exponential Backoff)</li>
 *   <li>에러 핸들링</li>
 * </ul>
 */
@Slf4j
@Configuration
public class KafkaConfig {

    /**
     * DLQ 토픽 접미사
     * 원본 토픽이 "user.events"인 경우 DLQ는 "user.events.DLQ"
     */
    private static final String DLQ_SUFFIX = ".DLQ";

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:boilerplate-group}")
    private String groupId;

    /**
     * Kafka Producer 설정
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Producer 최적화 설정
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // 모든 복제본이 응답할 때까지 대기
        config.put(ProducerConfig.RETRIES_CONFIG, 3); // 재시도 횟수
        config.put(ProducerConfig.LINGER_MS_CONFIG, 1); // 배치 전송 대기 시간
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // 압축 타입

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Kafka Consumer 설정
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Consumer 최적화 설정
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 처음부터 읽기

        // ⚠️ CRITICAL: Auto-commit 비활성화 - Manual ACK 사용
        // 이유: 메시지 처리 실패 시 offset이 자동 커밋되면 메시지 유실 발생
        // Manual ACK로 처리 성공 후에만 offset 커밋
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100); // 한 번에 가져올 레코드 수

        // JSON Deserializer 신뢰 패키지 설정
        // SECURITY: "*" 사용 시 Insecure Deserialization 취약점 발생
        // 신뢰할 수 있는 패키지만 명시적으로 지정
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.hamkkebu.boilerplate.*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.hamkkebu.boilerplate.data.event.BaseEvent");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // 동시성 설정
        factory.setConcurrency(3); // 동시 처리 스레드 수

        // Manual ACK 모드 설정
        // RECORD: 각 레코드 처리 후 개별 ACK
        // BATCH: 배치 전체 처리 후 ACK
        // MANUAL: 리스너에서 직접 acknowledge() 호출
        factory.getContainerProperties().setAckMode(
            org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL
        );

        // 에러 핸들러 설정 (DLQ + 재시도)
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    /**
     * Kafka 에러 핸들러 (DLQ + 재시도 정책)
     *
     * <p>재시도 정책: Exponential Backoff</p>
     * <ul>
     *   <li>초기 지연: 1초</li>
     *   <li>배수: 2배씩 증가</li>
     *   <li>최대 재시도: 3회</li>
     *   <li>총 재시도 시간: 1초 + 2초 + 4초 = 7초</li>
     * </ul>
     *
     * <p>3회 재시도 후 실패 시 DLQ로 전송</p>
     */
    @Bean
    public CommonErrorHandler errorHandler() {
        // DLQ Publisher 설정
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            kafkaTemplate(),
            (consumerRecord, exception) -> {
                // DLQ 토픽 결정: {원본토픽}.DLQ
                String dlqTopic = consumerRecord.topic() + DLQ_SUFFIX;
                log.error("Sending message to DLQ: topic={}, partition={}, offset={}, dlqTopic={}",
                    consumerRecord.topic(),
                    consumerRecord.partition(),
                    consumerRecord.offset(),
                    dlqTopic,
                    exception
                );
                return new TopicPartition(dlqTopic, consumerRecord.partition());
            }
        );

        // Exponential Backoff 설정
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);  // 1초
        backOff.setMultiplier(2.0);          // 2배씩 증가
        backOff.setMaxInterval(10000L);      // 최대 10초

        // DefaultErrorHandler 생성
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // 재시도하지 않을 예외 추가 (필요 시)
        // errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);

        // 재시도 로그
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("Retry attempt {} for topic={}, partition={}, offset={}",
                deliveryAttempt,
                record.topic(),
                record.partition(),
                record.offset()
            );
        });

        return errorHandler;
    }
}
