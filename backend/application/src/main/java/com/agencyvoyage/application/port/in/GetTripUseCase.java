package com.agencyvoyage.application.port.in;

import com.agencyvoyage.application.exception.TripNotFoundException;
import com.agencyvoyage.domain.trip.Trip;
import com.agencyvoyage.domain.trip.TripId;

public interface GetTripUseCase {

    /** @throws TripNotFoundException if no trip exists with that id */
    Trip getTrip(TripId id);
}
