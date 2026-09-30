package com.agencyvoyage.web;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;

/**
 * Boots the whole application (real REST controllers, real use-case wiring, real JPA
 * and Kafka adapters) against Testcontainers Postgres and Kafka. Singleton-container
 * pattern, as in the infrastructure module's own IT base classes - see
 * {@code AbstractPostgresIT} there for why it's started manually rather than via
 * {@code @Testcontainers}/{@code @Container}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractApiIT {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    static final ConfluentKafkaContainer KAFKA = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.7.1");

    static {
        POSTGRES.start();
        KAFKA.start();
    }

    @LocalServerPort
    protected int port;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }
}
