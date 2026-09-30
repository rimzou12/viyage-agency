package com.agencyvoyage.infrastructure.messaging.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Wire format for {@link com.agencyvoyage.application.port.out.event.GroupBookingFinalizedEvent}.
 */
public record GroupBookingFinalizedMessage(
        String bookingId,
        String tripId,
        String status,
        int participantCount,
        BigDecimal finalPricePerSeat,
        Instant occurredAt) {
}
