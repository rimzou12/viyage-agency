package com.agencyvoyage.domain.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.agencyvoyage.domain.user.UserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ContactMessageTest {

    private static final Instant NOW = Instant.parse("2027-01-01T00:00:00Z");

    @Test
    void createsAValidMessageFromACustomer() {
        UserId customerId = UserId.newId();
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(), customerId, customerId, "Alice", "alice@example.com", false,
                "Where is my seat?", NOW);

        assertThat(message.fromAdmin()).isFalse();
        assertThat(message.message()).isEqualTo("Where is my seat?");
    }

    @Test
    void createsAValidReplyFromAnAdmin() {
        UserId customerId = UserId.newId();
        UserId adminId = UserId.newId();
        ContactMessage reply = new ContactMessage(
                ContactMessageId.newId(), customerId, adminId, "Admin", "admin@example.com", true,
                "Row 12, seat A.", NOW);

        assertThat(reply.conversationUserId()).isEqualTo(customerId);
        assertThat(reply.authorUserId()).isEqualTo(adminId);
        assertThat(reply.fromAdmin()).isTrue();
    }

    @Test
    void rejectsABlankMessage() {
        UserId customerId = UserId.newId();
        assertThatThrownBy(() -> new ContactMessage(
                        ContactMessageId.newId(), customerId, customerId, "Alice", "alice@example.com", false, "  ",
                        NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsABlankAuthorName() {
        UserId customerId = UserId.newId();
        assertThatThrownBy(() -> new ContactMessage(
                        ContactMessageId.newId(), customerId, customerId, "  ", "alice@example.com", false, "Body",
                        NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
