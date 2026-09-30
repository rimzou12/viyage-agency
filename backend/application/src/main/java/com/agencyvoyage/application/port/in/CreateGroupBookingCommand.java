package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.trip.TripId;
import java.util.Objects;

public record CreateGroupBookingCommand(TripId tripId, String customerName) {

    public CreateGroupBookingCommand {
        Objects.requireNonNull(tripId, "tripId must not be null");
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("customerName must not be blank");
        }
    }
}
