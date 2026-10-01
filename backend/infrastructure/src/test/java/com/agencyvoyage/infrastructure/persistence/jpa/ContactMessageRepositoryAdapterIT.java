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
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                UserId.newId(),
                "Alice",
                "alice@example.com",
                "Help",
                "Where is my seat?",
                Instant.now().truncatedTo(ChronoUnit.MICROS));

        adapter.save(message);

        assertThat(adapter.findAll()).contains(message);
    }

    @Test
    void returnsMessagesNewestFirst() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        ContactMessage older = new ContactMessage(
                ContactMessageId.newId(), UserId.newId(), "Alice", "alice@example.com", "First", "First body", now);
        ContactMessage newer = new ContactMessage(
                ContactMessageId.newId(),
                UserId.newId(),
                "Bob",
                "bob@example.com",
                "Second",
                "Second body",
                now.plus(1, ChronoUnit.MINUTES));
        adapter.save(older);
        adapter.save(newer);

        List<ContactMessage> all = adapter.findAll();

        assertThat(all.indexOf(newer)).isLessThan(all.indexOf(older));
    }
}
