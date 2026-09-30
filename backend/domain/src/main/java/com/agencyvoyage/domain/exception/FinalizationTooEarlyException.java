package com.agencyvoyage.domain.exception;

import com.agencyvoyage.domain.booking.GroupBookingId;
import java.time.Instant;

public final class FinalizationTooEarlyException extends DomainException {

    public FinalizationTooEarlyException(GroupBookingId bookingId, Instant deadline, Instant now) {
        super("Group booking " + bookingId + " cannot be finalized before its deadline " + deadline + " (now " + now + ")");
    }
}
