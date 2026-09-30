package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.booking.GroupBookingId;
import java.util.Objects;

public record JoinGroupBookingCommand(GroupBookingId bookingId, String customerName) {

    public JoinGroupBookingCommand {
        Objects.requireNonNull(bookingId, "bookingId must not be null");
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("customerName must not be blank");
        }
    }
}
