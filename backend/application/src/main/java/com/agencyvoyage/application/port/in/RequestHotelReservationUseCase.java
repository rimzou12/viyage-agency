package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.booking.GroupBooking;

public interface RequestHotelReservationUseCase {

    GroupBooking requestHotelReservation(RequestHotelReservationCommand command);
}
