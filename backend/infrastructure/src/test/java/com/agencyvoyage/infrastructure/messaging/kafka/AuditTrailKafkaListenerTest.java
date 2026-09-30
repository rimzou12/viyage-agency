package com.agencyvoyage.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.agencyvoyage.application.port.out.AuditEntry;
import com.agencyvoyage.application.port.out.AuditEventType;
import com.agencyvoyage.application.port.out.AuditTrailRepository;
import com.agencyvoyage.domain.booking.GroupBookingStatus;
import com.agencyvoyage.infrastructure.messaging.kafka.dto.GroupBookingFinalizedMessage;
import com.agencyvoyage.infrastructure.messaging.kafka.dto.ParticipantJoinedMessage;
import com.agencyvoyage.infrastructure.messaging.kafka.dto.ParticipantLeftMessage;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditTrailKafkaListenerTest {

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @Captor
    private ArgumentCaptor<AuditEntry> entryCaptor;

    @Test
    void recordsAParticipantJoinedEntry() {
        AuditTrailKafkaListener listener = new AuditTrailKafkaListener(auditTrailRepository);
        String bookingId = UUID.randomUUID().toString();
        String participantId = UUID.randomUUID().toString();
        Instant occurredAt = Instant.parse("2027-01-01T00:00:00Z");

        listener.onParticipantJoined(new ParticipantJoinedMessage(
                bookingId, UUID.randomUUID().toString(), participantId, "Alice", 2, new BigDecimal("800"), occurredAt));

        verify(auditTrailRepository).append(entryCaptor.capture());
        AuditEntry entry = entryCaptor.getValue();
        assertThat(entry.bookingId().toString()).isEqualTo(bookingId);
        assertThat(entry.type()).isEqualTo(AuditEventType.PARTICIPANT_JOINED);
        assertThat(entry.participantId().toString()).isEqualTo(participantId);
        assertThat(entry.customerName()).isEqualTo("Alice");
        assertThat(entry.participantCount()).isEqualTo(2);
        assertThat(entry.pricePerSeat()).isEqualByComparingTo("800");
        assertThat(entry.status()).isNull();
        assertThat(entry.occurredAt()).isEqualTo(occurredAt);
    }

    @Test
    void recordsAParticipantLeftEntry() {
        AuditTrailKafkaListener listener = new AuditTrailKafkaListener(auditTrailRepository);
        String bookingId = UUID.randomUUID().toString();
        String participantId = UUID.randomUUID().toString();
        Instant occurredAt = Instant.parse("2027-01-01T00:00:00Z");

        listener.onParticipantLeft(new ParticipantLeftMessage(
                bookingId, UUID.randomUUID().toString(), participantId, 1, new BigDecimal("1000"), occurredAt));

        verify(auditTrailRepository).append(entryCaptor.capture());
        AuditEntry entry = entryCaptor.getValue();
        assertThat(entry.type()).isEqualTo(AuditEventType.PARTICIPANT_LEFT);
        assertThat(entry.participantId().toString()).isEqualTo(participantId);
        assertThat(entry.customerName()).isNull();
        assertThat(entry.participantCount()).isEqualTo(1);
    }

    @Test
    void recordsAFinalizedEntry() {
        AuditTrailKafkaListener listener = new AuditTrailKafkaListener(auditTrailRepository);
        String bookingId = UUID.randomUUID().toString();
        Instant occurredAt = Instant.parse("2027-01-01T00:00:00Z");

        listener.onFinalized(new GroupBookingFinalizedMessage(
                bookingId,
                UUID.randomUUID().toString(),
                GroupBookingStatus.CONFIRMED.name(),
                5,
                new BigDecimal("600"),
                occurredAt));

        verify(auditTrailRepository).append(entryCaptor.capture());
        AuditEntry entry = entryCaptor.getValue();
        assertThat(entry.type()).isEqualTo(AuditEventType.FINALIZED);
        assertThat(entry.participantId()).isNull();
        assertThat(entry.status()).isEqualTo(GroupBookingStatus.CONFIRMED);
        assertThat(entry.participantCount()).isEqualTo(5);
    }
}
