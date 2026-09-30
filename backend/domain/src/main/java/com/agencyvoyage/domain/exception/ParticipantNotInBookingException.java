package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.ParticipantId;

public final class ParticipantNotInBookingException extends DomainException {

    public ParticipantNotInBookingException(GroupBookingId bookingId, ParticipantId participantId) {
        super("Participant " + participantId + " is not part of group booking " + bookingId);
    }
}
