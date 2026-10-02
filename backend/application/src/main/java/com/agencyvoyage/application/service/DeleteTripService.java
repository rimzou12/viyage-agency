package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.NotAnAdminException;
import com.agencyvoyage.application.exception.TripNotFoundException;
import com.agencyvoyage.application.port.in.DeleteTripCommand;
import com.agencyvoyage.application.port.in.DeleteTripUseCase;
import com.agencyvoyage.application.port.out.TripRepository;
import java.util.Objects;

public final class DeleteTripService implements DeleteTripUseCase {

    private final TripRepository tripRepository;

    public DeleteTripService(TripRepository tripRepository) {
        this.tripRepository = Objects.requireNonNull(tripRepository, "tripRepository must not be null");
    }

    @Override
    public void deleteTrip(DeleteTripCommand command) {
        if (!command.requestedBy().isAdmin()) {
            throw new NotAnAdminException();
        }
        tripRepository
                .findById(command.tripId())
                .orElseThrow(() -> new TripNotFoundException(command.tripId()));

        tripRepository.deleteById(command.tripId());
    }
}
