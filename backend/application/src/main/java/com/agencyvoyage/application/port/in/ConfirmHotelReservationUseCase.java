package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.booking.GroupBooking;

public interface ConfirmHotelReservationUseCase {

    /** Confirms the reservation and emails every participant their confirmation. */
    GroupBooking confirmHotelReservation(ConfirmHotelReservationCommand command);
}
