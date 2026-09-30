package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.user.UserId;

public final class AlreadyJoinedException extends DomainException {

    public AlreadyJoinedException(GroupBookingId bookingId, UserId userId) {
        super("User " + userId + " has already joined group booking " + bookingId);
    }
}
