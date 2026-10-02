package com.agencyvoyage.application.port.in;

/** Only an admin may remove a hotel from a trip's catalog. */
public interface DeleteHotelUseCase {

    void deleteHotel(DeleteHotelCommand command);
}
