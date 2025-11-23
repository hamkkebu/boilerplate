package com.hamkkebu.boilerplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Hamkkebu Boilerplate Application
 *
 * Main application class for the Hamkkebu boilerplate project.
 * Includes support for:
 * - JPA Auditing (configured in JpaAuditingConfig)
 * - Kafka messaging
 * - Scheduled tasks (Outbox pattern)
 */
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class BoilerplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoilerplateApplication.class, args);
    }
}
