package com.agencyvoyage.application.service;

import com.agencyvoyage.application.port.in.ListHotelsForTripUseCase;
import com.agencyvoyage.application.port.out.HotelRepository;
import com.agencyvoyage.domain.hotel.Hotel;
import com.agencyvoyage.domain.trip.TripId;
import java.util.List;
import java.util.Objects;

public final class ListHotelsForTripService implements ListHotelsForTripUseCase {

    private final HotelRepository hotelRepository;

    public ListHotelsForTripService(HotelRepository hotelRepository) {
        this.hotelRepository = Objects.requireNonNull(hotelRepository, "hotelRepository must not be null");
    }

    @Override
    public List<Hotel> listHotels(TripId tripId) {
        return hotelRepository.findByTripId(tripId);
    }
}
