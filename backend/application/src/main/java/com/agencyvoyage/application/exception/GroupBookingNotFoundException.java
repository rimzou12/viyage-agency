package com.agencyvoyage.application.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;

public final class GroupBookingNotFoundException extends RuntimeException {

    public GroupBookingNotFoundException(GroupBookingId id) {
        super("No group booking found with id " + id);
    }
}
