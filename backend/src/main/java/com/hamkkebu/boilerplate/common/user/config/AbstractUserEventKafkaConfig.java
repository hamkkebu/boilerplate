package com.hamkkebu.boilerplate.common.user.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 이벤트 Kafka Consumer 설정 추상 클래스
 *
 * <p>사용자 이벤트(USER_REGISTERED, USER_DELETED)를 Map으로 수신하여
 * 이벤트 타입에 따라 처리합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Configuration
 * public class LedgerKafkaConfig extends AbstractUserEventKafkaConfig {
 *
 *     @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
 *     private String bootstrapServers;
 *
 *     @Override
 *     protected String getBootstrapServers() {
 *         return bootstrapServers;
 *     }
 *
 *     @Override
 *     protected String getGroupId() {
 *         return "ledger-service-group";
 *     }
 *
 *     @Override
 *     protected String getConsumerFactoryBeanName() {
 *         return "ledgerUserEventConsumerFactory";
 *     }
 *
 *     @Override
 *     protected String getContainerFactoryBeanName() {
 *         return "ledgerKafkaListenerContainerFactory";
 *     }
 *
 *     @Bean
 *     public ConsumerFactory<String, Map<String, Object>> ledgerUserEventConsumerFactory() {
 *         return createConsumerFactory();
 *     }
 *
 *     @Bean
 *     public ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>>
 *             ledgerKafkaListenerContainerFactory() {
 *         return createContainerFactory();
 *     }
 * }
 * }
 * </pre>
 */
public abstract class AbstractUserEventKafkaConfig {

    /**
     * Kafka 부트스트랩 서버 주소 반환
     */
    protected abstract String getBootstrapServers();

    /**
     * Consumer 그룹 ID 반환
     */
    protected abstract String getGroupId();

    /**
     * ConsumerFactory 생성
     */
    protected ConsumerFactory<String, Map<String, Object>> createConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // StringSerializer로 보낸 JSON을 Map으로 역직렬화
        // 타입 헤더가 없으므로 기본 타입을 지정해야 함
        JsonDeserializer<Map<String, Object>> deserializer = new JsonDeserializer<>(
            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
        );
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    /**
     * ConcurrentKafkaListenerContainerFactory 생성
     */
    protected ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> createContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createConsumerFactory());
        return factory;
    }
}
