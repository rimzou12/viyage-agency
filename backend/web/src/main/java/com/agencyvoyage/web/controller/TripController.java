package com.agencyvoyage.web.controller;

import com.agencyvoyage.application.port.in.GetTripUseCase;
import com.agencyvoyage.application.port.in.ListTripsUseCase;
import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.web.dto.TripResponse;
import java.util.List;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final ListTripsUseCase listTripsUseCase;
    private final GetTripUseCase getTripUseCase;

    public TripController(ListTripsUseCase listTripsUseCase, GetTripUseCase getTripUseCase) {
        this.listTripsUseCase = Objects.requireNonNull(listTripsUseCase);
        this.getTripUseCase = Objects.requireNonNull(getTripUseCase);
    }

    @GetMapping
    public List<TripResponse> listTrips() {
        return listTripsUseCase.listTrips().stream().map(TripResponse::from).toList();
    }

    @GetMapping("/{tripId}")
    public TripResponse getTrip(@PathVariable String tripId) {
        return TripResponse.from(getTripUseCase.getTrip(TripId.of(tripId)));
    }
}
