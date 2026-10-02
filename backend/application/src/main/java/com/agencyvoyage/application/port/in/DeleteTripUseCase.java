package com.agencyvoyage.application.port.in;

/** Only an admin may remove a trip from the catalog. */
public interface DeleteTripUseCase {

    void deleteTrip(DeleteTripCommand command);
}
