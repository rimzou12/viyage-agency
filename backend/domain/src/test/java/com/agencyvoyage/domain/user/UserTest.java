package com.agencyvoyage.domain.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void rejectsAnInvalidEmail() {
        assertThatThrownBy(() -> new User(UserId.newId(), "not-an-email", "Alice"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }

    @Test
    void rejectsABlankDisplayName() {
        assertThatThrownBy(() -> new User(UserId.newId(), "alice@example.com", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("displayName");
    }
}
