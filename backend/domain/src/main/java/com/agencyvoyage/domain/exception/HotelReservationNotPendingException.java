package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.HotelReservationStatus;

/** Thrown when confirming a hotel reservation that was never requested, or is already confirmed. */
public final class HotelReservationNotPendingException extends DomainException {

    public HotelReservationNotPendingException(GroupBookingId bookingId, HotelReservationStatus status) {
        super("Hotel reservation for group booking " + bookingId + " is " + status + ", not PENDING");
    }
}
