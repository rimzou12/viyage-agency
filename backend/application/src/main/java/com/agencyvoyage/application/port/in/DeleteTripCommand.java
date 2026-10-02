package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.domain.user.User;
import java.util.Objects;

public record DeleteTripCommand(TripId tripId, User requestedBy) {

    public DeleteTripCommand {
        Objects.requireNonNull(tripId, "tripId must not be null");
        Objects.requireNonNull(requestedBy, "requestedBy must not be null");
    }
}
