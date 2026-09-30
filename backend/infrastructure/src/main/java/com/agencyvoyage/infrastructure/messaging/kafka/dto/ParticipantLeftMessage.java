package com.agencyvoyage.infrastructure.messaging.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ParticipantLeftMessage(
        String bookingId,
        String tripId,
        String participantId,
        int participantCount,
        BigDecimal pricePerSeat,
        Instant occurredAt) {
}
