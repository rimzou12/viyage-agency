package com.agencyvoyage.application.exception;

import com.agencyvoyage.domain.trip.TripId;

public final class TripNotFoundException extends RuntimeException {

    public TripNotFoundException(TripId id) {
        super("No trip found with id " + id);
    }
}
