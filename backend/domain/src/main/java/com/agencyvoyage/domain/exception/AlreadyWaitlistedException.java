package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.user.UserId;

public final class AlreadyWaitlistedException extends DomainException {

    public AlreadyWaitlistedException(GroupBookingId bookingId, UserId userId) {
        super("User " + userId + " is already on the waitlist for group booking " + bookingId);
    }
}
