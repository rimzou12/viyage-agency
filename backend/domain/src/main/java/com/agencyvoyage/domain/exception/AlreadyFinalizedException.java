package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;

public final class AlreadyFinalizedException extends DomainException {

    public AlreadyFinalizedException(GroupBookingId bookingId, GroupBookingStatus status) {
        super("Group booking " + bookingId + " was already finalized as " + status);
    }
}
