package com.agencyvoyage.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.application.port.out.UserAccount;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.infrastructure.config.AbstractPostgresIT;
import com.agencyvoyage.infrastructure.persistence.jpa.adapter.UserRepositoryAdapter;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserRepositoryAdapterIT extends AbstractPostgresIT {

    @Autowired
    private UserRepositoryAdapter adapter;

    @Test
    void createsAndFindsAnAccountByEmail() {
        User created = adapter.createAccount("alice@example.com", "hashed-password", "Alice");

        Optional<UserAccount> found = adapter.findAccountByEmail("alice@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().user()).isEqualTo(created);
        assertThat(found.get().hashedPassword()).isEqualTo("hashed-password");
    }

    @Test
    void findsByIdAfterCreation() {
        User created = adapter.createAccount("bob@example.com", "hashed-password", "Bob");

        assertThat(adapter.findById(created.id())).contains(created);
    }

    @Test
    void existsByEmailReflectsWhatWasCreated() {
        assertThat(adapter.existsByEmail("carol@example.com")).isFalse();

        adapter.createAccount("carol@example.com", "hashed-password", "Carol");

        assertThat(adapter.existsByEmail("carol@example.com")).isTrue();
    }

    @Test
    void findAccountByEmailIsEmptyForAnUnknownEmail() {
        assertThat(adapter.findAccountByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    void persistsAndReloadsTheAdminFlag() {
        User created = adapter.createAccount("admin@example.com", "hashed-password", "Admin", true);

        assertThat(created.isAdmin()).isTrue();
        assertThat(adapter.findById(created.id())).contains(created);
    }
}
