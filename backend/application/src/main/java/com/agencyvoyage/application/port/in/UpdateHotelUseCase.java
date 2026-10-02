package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.hotel.Hotel;

/** Only an admin may edit the hotel catalog. */
public interface UpdateHotelUseCase {

    Hotel updateHotel(UpdateHotelCommand command);
}
