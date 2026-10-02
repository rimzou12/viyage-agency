package com.agencyvoyage.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.support.ContactMessageId;
import com.agencyvoyage.domain.user.UserId;
import com.agencyvoyage.infrastructure.config.AbstractPostgresIT;
import com.agencyvoyage.infrastructure.persistence.jpa.adapter.ContactMessageRepositoryAdapter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ContactMessageRepositoryAdapterIT extends AbstractPostgresIT {

    @Autowired
    private ContactMessageRepositoryAdapter adapter;

    @Test
    void savesAndReloadsAMessage() {
        // Postgres' timestamp column stores microsecond precision; Instant.now() carries
        // nanoseconds, so round-tripping an untruncated instant would never compare equal.
        UserId customerId = UserId.newId();
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                customerId,
                customerId,
                "Alice",
                "alice@example.com",
                false,
                "Where is my seat?",
                Instant.now().truncatedTo(ChronoUnit.MICROS));

        adapter.save(message);

        assertThat(adapter.findAll()).contains(message);
    }

    @Test
    void returnsMessagesNewestFirstAcrossAllConversations() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        UserId aliceId = UserId.newId();
        UserId bobId = UserId.newId();
        ContactMessage older =
                new ContactMessage(ContactMessageId.newId(), aliceId, aliceId, "Alice", "alice@example.com", false, "First body", now);
        ContactMessage newer = new ContactMessage(
                ContactMessageId.newId(),
                bobId,
                bobId,
                "Bob",
                "bob@example.com",
                false,
                "Second body",
                now.plus(1, ChronoUnit.MINUTES));
        adapter.save(older);
        adapter.save(newer);

        List<ContactMessage> all = adapter.findAll();

        assertThat(all.indexOf(newer)).isLessThan(all.indexOf(older));
    }

    @Test
    void returnsOneCustomersConversationOldestFirstIncludingAdminReplies() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        UserId customerId = UserId.newId();
        UserId adminId = UserId.newId();
        UserId otherCustomerId = UserId.newId();

        ContactMessage firstMessage = new ContactMessage(
                ContactMessageId.newId(), customerId, customerId, "Alice", "alice@example.com", false, "Where is my seat?", now);
        ContactMessage adminReply = new ContactMessage(
                ContactMessageId.newId(),
                customerId,
                adminId,
                "Admin",
                "admin@example.com",
                true,
                "Seat 12A",
                now.plus(1, ChronoUnit.MINUTES));
        ContactMessage unrelatedMessage = new ContactMessage(
                ContactMessageId.newId(),
                otherCustomerId,
                otherCustomerId,
                "Bob",
                "bob@example.com",
                false,
                "Different thread",
                now);
        adapter.save(firstMessage);
        adapter.save(adminReply);
        adapter.save(unrelatedMessage);

        List<ContactMessage> conversation = adapter.findByConversationUserId(customerId);

        assertThat(conversation).containsExactly(firstMessage, adminReply);
    }
}
