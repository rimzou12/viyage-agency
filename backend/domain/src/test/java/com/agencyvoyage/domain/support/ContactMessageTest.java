package com.agencyvoyage.domain.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.agencyvoyage.domain.user.UserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ContactMessageTest {

    private static final Instant NOW = Instant.parse("2027-01-01T00:00:00Z");

    @Test
    void createsAValidMessage() {
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(), UserId.newId(), "Alice", "alice@example.com", "Help", "Where is my seat?", NOW);

        assertThat(message.subject()).isEqualTo("Help");
    }

    @Test
    void rejectsABlankSubject() {
        assertThatThrownBy(() -> new ContactMessage(
                        ContactMessageId.newId(), UserId.newId(), "Alice", "alice@example.com", "  ", "Body", NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsABlankMessage() {
        assertThatThrownBy(() -> new ContactMessage(
                        ContactMessageId.newId(), UserId.newId(), "Alice", "alice@example.com", "Help", "  ", NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
