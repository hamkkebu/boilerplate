package com.hamkkebu.boilerplate.common.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * TestContainers 설정
 *
 * <p>통합 테스트를 위한 Docker 컨테이너를 자동으로 시작하고 설정합니다.</p>
 *
 * <p>제공하는 컨테이너:</p>
 * <ul>
 *   <li>MySQL: 데이터베이스</li>
 *   <li>Kafka: 메시지 브로커</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @IntegrationTest
 * @Import(TestContainerConfig.class)
 * class SampleServiceTest {
 *     // 테스트 코드...
 * }
 * }
 * </pre>
 */
@Slf4j
@TestConfiguration
public class TestContainerConfig {

    private static final String MYSQL_IMAGE = "mysql:8.0";
    private static final String KAFKA_IMAGE = "confluentinc/cp-kafka:7.5.0";

    /**
     * MySQL 컨테이너
     * <p>싱글톤 패턴으로 모든 테스트에서 공유됩니다.</p>
     */
    @Bean
    public MySQLContainer<?> mySQLContainer() {
        MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE))
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);  // 컨테이너 재사용으로 속도 향상

        container.start();

        log.info("MySQL Container started:");
        log.info("  - JDBC URL: {}", container.getJdbcUrl());
        log.info("  - Username: {}", container.getUsername());
        log.info("  - Password: {}", container.getPassword());

        return container;
    }

    /**
     * Kafka 컨테이너
     * <p>싱글톤 패턴으로 모든 테스트에서 공유됩니다.</p>
     */
    @Bean
    public KafkaContainer kafkaContainer() {
        KafkaContainer container = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE))
            .withReuse(true);  // 컨테이너 재사용으로 속도 향상

        container.start();

        log.info("Kafka Container started:");
        log.info("  - Bootstrap Servers: {}", container.getBootstrapServers());

        return container;
    }

    /**
     * 동적 프로퍼티 설정
     * <p>컨테이너의 동적 포트를 Spring 프로퍼티에 주입합니다.</p>
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // MySQL이 이미 시작되어 있으므로 직접 참조할 수 없음
        // 대신 application-test.yml에서 설정하거나, 별도로 처리 필요
    }
}
