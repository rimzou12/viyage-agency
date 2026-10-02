package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.trip.Trip;

/** Only an admin may edit the trip catalog. */
public interface UpdateTripUseCase {

    Trip updateTrip(UpdateTripCommand command);
}
