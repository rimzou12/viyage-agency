package com.agencyvoyage.infrastructure.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Minimal Spring Boot bootstrap for the persistence-adapter integration tests: this
 * module has no {@code @SpringBootApplication} of its own (that lives in {@code web}),
 * so tests need a stand-in to enable JPA autoconfiguration and component scanning.
 * Entity/repository packages are scanned explicitly rather than relying on
 * {@code @EnableAutoConfiguration}'s package inference, which would only cover this
 * class's own package, not the sibling {@code persistence.jpa} package. Scanning is
 * deliberately scoped to {@code persistence.jpa} only - the sibling
 * {@code messaging.kafka} and {@code scheduling} packages need a Kafka broker and a
 * {@code FinalizeGroupBookingUseCase} bean respectively, neither of which this
 * persistence-only context provides; see {@code KafkaTestApplication} for those.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.agencyvoyage.infrastructure.persistence.jpa")
@EntityScan(basePackages = "com.agencyvoyage.infrastructure.persistence.jpa.entity")
@EnableJpaRepositories(basePackages = "com.agencyvoyage.infrastructure.persistence.jpa.repository")
public class InfrastructureTestApplication {
}
