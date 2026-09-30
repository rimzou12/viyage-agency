package com.agencyvoyage.infrastructure.messaging.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Wire format for {@link com.agencyvoyage.application.port.out.event.ParticipantJoinedEvent}:
 * flat and string-typed so the topic's schema doesn't leak domain/application types.
 */
public record ParticipantJoinedMessage(
        String bookingId,
        String tripId,
        String participantId,
        String customerName,
        int participantCount,
        BigDecimal pricePerSeat,
        Instant occurredAt) {
}
