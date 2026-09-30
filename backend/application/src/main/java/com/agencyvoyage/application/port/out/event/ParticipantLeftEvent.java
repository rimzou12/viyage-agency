package com.agencyvoyage.application.port.out.event;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.ParticipantId;
import com.agencyvoyage.domain.trip.TripId;
import java.math.BigDecimal;
import java.time.Instant;

public record ParticipantLeftEvent(
        GroupBookingId bookingId,
        TripId tripId,
        ParticipantId participantId,
        int participantCount,
        BigDecimal pricePerSeat,
        Instant occurredAt) {
}
