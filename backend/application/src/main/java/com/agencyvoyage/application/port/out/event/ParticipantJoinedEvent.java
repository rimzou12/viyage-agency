package com.agencyvoyage.application.port.out.event;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.ParticipantId;
import com.agencyvoyage.domain.trip.TripId;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Raised whenever a participant joins a group booking (including the creator joining
 * at creation time). Carries the resulting price so downstream consumers don't need
 * to recompute it.
 */
public record ParticipantJoinedEvent(
        GroupBookingId bookingId,
        TripId tripId,
        ParticipantId participantId,
        String customerName,
        int participantCount,
        BigDecimal pricePerSeat,
        Instant occurredAt) {
}
