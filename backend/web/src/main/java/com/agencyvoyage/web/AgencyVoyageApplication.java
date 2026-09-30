package com.agencyvoyage.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.agencyvoyage")
@EntityScan(basePackages = "com.agencyvoyage.infrastructure.persistence.jpa.entity")
@EnableJpaRepositories(basePackages = "com.agencyvoyage.infrastructure.persistence.jpa.repository")
@EnableScheduling
public class AgencyVoyageApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgencyVoyageApplication.class, args);
    }
}
