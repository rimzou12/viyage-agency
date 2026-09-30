package com.agencyvoyage.application.port.out.event;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;
import com.agencyvoyage.domain.trip.TripId;
import java.math.BigDecimal;
import java.time.Instant;

public record GroupBookingFinalizedEvent(
        GroupBookingId bookingId,
        TripId tripId,
        GroupBookingStatus status,
        int participantCount,
        BigDecimal finalPricePerSeat,
        Instant occurredAt) {
}
