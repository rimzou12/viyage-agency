package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.user.UserId;

public final class NotOnWaitlistException extends DomainException {

    public NotOnWaitlistException(GroupBookingId bookingId, UserId userId) {
        super("User " + userId + " is not on the waitlist for group booking " + bookingId);
    }
}
