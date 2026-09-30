package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.TripNotFoundException;
import com.agencyvoyage.application.port.in.GetTripUseCase;
import com.agencyvoyage.application.port.in.ListTripsUseCase;
import com.agencyvoyage.application.port.out.TripRepository;
import com.agencyvoyage.domain.trip.Trip;
import com.agencyvoyage.domain.trip.TripId;
import java.util.List;
import java.util.Objects;

public final class TripQueryService implements ListTripsUseCase, GetTripUseCase {

    private final TripRepository tripRepository;

    public TripQueryService(TripRepository tripRepository) {
        this.tripRepository = Objects.requireNonNull(tripRepository, "tripRepository must not be null");
    }

    @Override
    public List<Trip> listTrips() {
        return tripRepository.findAll();
    }

    @Override
    public Trip getTrip(TripId id) {
        return tripRepository.findById(id).orElseThrow(() -> new TripNotFoundException(id));
    }
}
