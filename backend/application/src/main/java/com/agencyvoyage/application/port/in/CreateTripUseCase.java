package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.trip.Trip;

/** Only an admin may add to the trip catalog. */
public interface CreateTripUseCase {

    Trip createTrip(CreateTripCommand command);
}
