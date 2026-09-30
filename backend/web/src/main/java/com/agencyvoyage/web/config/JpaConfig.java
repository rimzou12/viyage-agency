package com.agencyvoyage.web.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Kept as its own {@code @Configuration} rather than on {@link com.agencyvoyage.web.AgencyVoyageApplication}
 * directly: {@code @WebMvcTest} uses the app class as its context source but filters out
 * plain configuration classes, so putting {@code @EnableJpaRepositories}/{@code @EntityScan}
 * here keeps MVC slice tests from dragging in the full JPA stack.
 */
@Configuration
@EntityScan(basePackages = "com.agencyvoyage.infrastructure.persistence.jpa.entity")
@EnableJpaRepositories(basePackages = "com.agencyvoyage.infrastructure.persistence.jpa.repository")
public class JpaConfig {
}
