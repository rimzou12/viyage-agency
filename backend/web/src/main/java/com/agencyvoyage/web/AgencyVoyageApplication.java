package com.agencyvoyage.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.agencyvoyage")
@EnableScheduling
public class AgencyVoyageApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgencyVoyageApplication.class, args);
    }
}
