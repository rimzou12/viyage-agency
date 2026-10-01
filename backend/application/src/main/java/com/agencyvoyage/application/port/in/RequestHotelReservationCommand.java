package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.booking.GroupBookingId;
import java.util.Objects;

public record RequestHotelReservationCommand(GroupBookingId bookingId, String reference) {

    public RequestHotelReservationCommand {
        Objects.requireNonNull(bookingId, "bookingId must not be null");
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("reference must not be blank");
        }
    }
}
