package com.agencyvoyage.infrastructure.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Bootstrap for the Kafka adapter integration tests - see {@link InfrastructureTestApplication}.
 * JPA/DataSource autoconfiguration is excluded: it activates purely because spring-data-jpa
 * is on this module's classpath (autoconfiguration is classpath-driven, not scan-scoped),
 * and there is no datasource configured for this Kafka-only context.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        DataJpaRepositoriesAutoConfiguration.class
})
@ComponentScan(basePackages = "com.agencyvoyage.infrastructure.messaging.kafka")
public class KafkaTestApplication {
}
