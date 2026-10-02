package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.trip.Trip;
import com.agencyvoyage.domain.trip.TripId;
import java.util.List;
import java.util.Optional;

public interface TripRepository {

    void save(Trip trip);

    Optional<Trip> findById(TripId id);

    List<Trip> findAll();

    void deleteById(TripId id);
}
