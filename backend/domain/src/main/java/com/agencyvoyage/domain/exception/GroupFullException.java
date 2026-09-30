package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;

public final class GroupFullException extends DomainException {

    public GroupFullException(GroupBookingId bookingId, int maxParticipants) {
        super("Group booking " + bookingId + " already has the maximum of " + maxParticipants + " participants");
    }
}
