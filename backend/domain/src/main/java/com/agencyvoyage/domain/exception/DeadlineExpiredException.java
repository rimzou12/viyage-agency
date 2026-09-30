package com.agencyvoyage.domain.exception;

import java.time.Instant;

public final class DeadlineExpiredException extends DomainException {

    public DeadlineExpiredException(Instant deadline, Instant now) {
        super("Booking deadline " + deadline + " has already passed (now " + now + ")");
    }
}
