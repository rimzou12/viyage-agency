package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.hotel.Hotel;
import com.agencyvoyage.domain.trip.TripId;
import java.util.List;

public interface ListHotelsForTripUseCase {

    List<Hotel> listHotels(TripId tripId);
}
