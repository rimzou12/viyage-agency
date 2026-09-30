package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;

public final class BookingClosedException extends DomainException {

    public BookingClosedException(GroupBookingId bookingId, GroupBookingStatus status) {
        super("Group booking " + bookingId + " is " + status + " and no longer accepts participants");
    }
}
