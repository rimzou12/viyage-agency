package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.booking.GroupBookingId;
import java.util.Objects;

public record ConfirmHotelReservationCommand(GroupBookingId bookingId) {

    public ConfirmHotelReservationCommand {
        Objects.requireNonNull(bookingId, "bookingId must not be null");
    }
}
