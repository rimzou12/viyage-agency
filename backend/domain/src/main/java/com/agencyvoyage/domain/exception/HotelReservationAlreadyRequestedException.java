package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;

public final class HotelReservationAlreadyRequestedException extends DomainException {

    public HotelReservationAlreadyRequestedException(GroupBookingId bookingId) {
        super("A hotel reservation was already requested for group booking " + bookingId);
    }
}
