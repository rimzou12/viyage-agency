package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.trip.Trip;
import java.util.List;

public interface ListTripsUseCase {

    List<Trip> listTrips();
}
