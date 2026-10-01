package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.ParticipantId;

/** Thrown when joining with a referrer who isn't actually a participant in this booking. */
public final class InvalidReferralException extends DomainException {

    public InvalidReferralException(GroupBookingId bookingId, ParticipantId referrerParticipantId) {
        super("Referrer " + referrerParticipantId + " is not a participant in group booking " + bookingId);
    }
}
