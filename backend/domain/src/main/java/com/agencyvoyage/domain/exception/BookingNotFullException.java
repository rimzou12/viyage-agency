package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;

/** Thrown when joining the waitlist is attempted on a booking that still has open seats. */
public final class BookingNotFullException extends DomainException {

    public BookingNotFullException(GroupBookingId bookingId) {
        super("Group booking " + bookingId + " is not full yet - join it directly instead of waitlisting");
    }
}
