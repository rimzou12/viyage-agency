package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.user.UserId;
import java.util.Objects;

public record LeaveGroupBookingCommand(GroupBookingId bookingId, UserId userId) {

    public LeaveGroupBookingCommand {
        Objects.requireNonNull(bookingId, "bookingId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
    }
}
