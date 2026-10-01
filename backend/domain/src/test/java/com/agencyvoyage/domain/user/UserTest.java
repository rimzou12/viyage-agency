package com.agencyvoyage.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void defaultsToNotAnAdmin() {
        User user = new User(UserId.newId(), "alice@example.com", "Alice");

        assertThat(user.isAdmin()).isFalse();
    }

    @Test
    void canBeCreatedAsAnAdmin() {
        User admin = new User(UserId.newId(), "admin@example.com", "Admin", true);

        assertThat(admin.isAdmin()).isTrue();
    }
}
