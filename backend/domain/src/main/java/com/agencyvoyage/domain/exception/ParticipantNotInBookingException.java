package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.user.UserId;

public final class ParticipantNotInBookingException extends DomainException {

    public ParticipantNotInBookingException(GroupBookingId bookingId, UserId userId) {
        super("User " + userId + " is not a participant in group booking " + bookingId);
    }
}
