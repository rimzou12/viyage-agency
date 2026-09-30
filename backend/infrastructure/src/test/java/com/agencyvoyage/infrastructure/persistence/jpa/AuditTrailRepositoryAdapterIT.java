package com.agencyvoyage.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.application.port.out.AuditEntry;
import com.agencyvoyage.application.port.out.AuditEventType;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;
import com.agencyvoyage.domain.booking.ParticipantId;
import com.agencyvoyage.infrastructure.config.AbstractPostgresIT;
import com.agencyvoyage.infrastructure.persistence.jpa.adapter.AuditTrailRepositoryAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AuditTrailRepositoryAdapterIT extends AbstractPostgresIT {

    @Autowired
    private AuditTrailRepositoryAdapter adapter;

    @Test
    void returnsAnEmptyHistoryForAnUnknownBooking() {
        assertThat(adapter.findByBookingId(GroupBookingId.newId())).isEmpty();
    }

    @Test
    void returnsAppendedEntriesOldestFirstAndScopedToTheirBooking() {
        GroupBookingId bookingId = GroupBookingId.newId();
        GroupBookingId otherBookingId = GroupBookingId.newId();
        Instant t1 = Instant.parse("2027-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2027-01-01T00:10:00Z");
        Instant t3 = Instant.parse("2027-01-01T00:20:00Z");

        // Appended out of chronological order to prove ordering comes from occurredAt, not insertion order.
        adapter.append(joinedEntry(bookingId, t2));
        adapter.append(finalizedEntry(bookingId, t3));
        adapter.append(joinedEntry(bookingId, t1));
        adapter.append(joinedEntry(otherBookingId, t1));

        List<AuditEntry> history = adapter.findByBookingId(bookingId);

        assertThat(history).hasSize(3);
        assertThat(history).extracting(AuditEntry::occurredAt).containsExactly(t1, t2, t3);
        assertThat(history.get(2).type()).isEqualTo(AuditEventType.FINALIZED);
        assertThat(history.get(2).status()).isEqualTo(GroupBookingStatus.CONFIRMED);
    }

    private static AuditEntry joinedEntry(GroupBookingId bookingId, Instant occurredAt) {
        return new AuditEntry(
                bookingId,
                AuditEventType.PARTICIPANT_JOINED,
                ParticipantId.newId(),
                "Alice",
                1,
                new BigDecimal("1000"),
                null,
                occurredAt);
    }

    private static AuditEntry finalizedEntry(GroupBookingId bookingId, Instant occurredAt) {
        return new AuditEntry(
                bookingId,
                AuditEventType.FINALIZED,
                null,
                null,
                3,
                new BigDecimal("800"),
                GroupBookingStatus.CONFIRMED,
                occurredAt);
    }
}
