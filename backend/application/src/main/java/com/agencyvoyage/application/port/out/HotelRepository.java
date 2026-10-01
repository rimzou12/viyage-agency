package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.hotel.Hotel;
import com.agencyvoyage.domain.hotel.HotelId;
import com.agencyvoyage.domain.trip.TripId;
import java.util.List;
import java.util.Optional;

public interface HotelRepository {

    void save(Hotel hotel);

    Optional<Hotel> findById(HotelId id);

    List<Hotel> findByTripId(TripId tripId);

    void deleteById(HotelId id);
}
