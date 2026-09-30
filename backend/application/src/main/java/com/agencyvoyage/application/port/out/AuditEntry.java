package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;
import com.agencyvoyage.domain.booking.ParticipantId;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * One entry in a group booking's history, built from its Kafka event stream
 * ({@code PARTICIPANT_JOINED}/{@code PARTICIPANT_LEFT}/{@code FINALIZED}). Fields that
 * don't apply to a given {@link #type()} are null: {@code participantId}/
 * {@code customerName} only on join/leave, {@code status} only on finalize.
 */
public record AuditEntry(
        GroupBookingId bookingId,
        AuditEventType type,
        ParticipantId participantId,
        String customerName,
        int participantCount,
        BigDecimal pricePerSeat,
        GroupBookingStatus status,
        Instant occurredAt) {
}
