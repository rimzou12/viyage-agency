package com.agencyvoyage.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void hashedPasswordMatchesTheOriginal() {
        String hashed = hasher.hash("password123");

        assertThat(hashed).isNotEqualTo("password123");
        assertThat(hasher.matches("password123", hashed)).isTrue();
    }

    @Test
    void wrongPasswordDoesNotMatch() {
        String hashed = hasher.hash("password123");

        assertThat(hasher.matches("wrong-password", hashed)).isFalse();
    }
}
