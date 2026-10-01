package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.hotel.Hotel;

/** Only an admin may curate the hotel catalog for a trip. */
public interface AddHotelUseCase {

    Hotel addHotel(AddHotelCommand command);
}
