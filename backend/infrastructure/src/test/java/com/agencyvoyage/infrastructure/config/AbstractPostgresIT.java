package com.agencyvoyage.infrastructure.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton-container pattern: the Postgres container is started once, manually, for
 * the whole JVM and never stopped explicitly (Testcontainers' Ryuk reaper cleans it up
 * when the test process exits). Letting {@code @Testcontainers}/{@code @Container}
 * manage a container declared on this shared base class would stop it after the first
 * subclass's tests, breaking the next subclass that reuses the same static field.
 */
@SpringBootTest(classes = InfrastructureTestApplication.class)
public abstract class AbstractPostgresIT {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}
