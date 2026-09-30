package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.domain.user.User;
import java.util.Objects;

public record CreateGroupBookingCommand(TripId tripId, User actingUser) {

    public CreateGroupBookingCommand {
        Objects.requireNonNull(tripId, "tripId must not be null");
        Objects.requireNonNull(actingUser, "actingUser must not be null");
    }
}
