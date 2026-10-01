package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;

/** Thrown when a hotel reservation is requested before the group itself is confirmed. */
public final class BookingNotConfirmedException extends DomainException {

    public BookingNotConfirmedException(GroupBookingId bookingId, GroupBookingStatus status) {
        super("Group booking " + bookingId + " is " + status + ", not CONFIRMED - the hotel can't be reserved yet");
    }
}
