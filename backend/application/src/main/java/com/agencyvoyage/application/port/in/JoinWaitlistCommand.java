package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.user.User;
import java.util.Objects;

public record JoinWaitlistCommand(GroupBookingId bookingId, User actingUser) {

    public JoinWaitlistCommand {
        Objects.requireNonNull(bookingId, "bookingId must not be null");
        Objects.requireNonNull(actingUser, "actingUser must not be null");
    }
}
