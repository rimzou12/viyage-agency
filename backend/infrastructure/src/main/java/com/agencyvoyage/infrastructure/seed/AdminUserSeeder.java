package com.agencyvoyage.infrastructure.seed;

import com.agencyvoyage.application.port.out.PasswordHasher;
import com.agencyvoyage.application.port.out.UserRepository;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds one admin account on startup if it doesn't exist yet, so there's a way to sign
 * in as an admin without a dedicated admin-registration flow. Dev-only credentials by
 * default - see {@code agency-voyage.admin-seed} in application.yml.
 */
@Component
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final String adminEmail;
    private final String adminPassword;

    public AdminUserSeeder(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            @Value("${agency-voyage.admin-seed.email}") String adminEmail,
            @Value("${agency-voyage.admin-seed.password}") String adminPassword) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
        this.adminEmail = Objects.requireNonNull(adminEmail, "adminEmail must not be null");
        this.adminPassword = Objects.requireNonNull(adminPassword, "adminPassword must not be null");
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        userRepository.createAccount(adminEmail, passwordHasher.hash(adminPassword), "Admin", true);
    }
}
