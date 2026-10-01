package com.agencyvoyage.domain.booking;

public enum HotelReservationStatus {
    /** No one has submitted a hotel reservation reference yet. */
    NOT_REQUESTED,
    /** A participant submitted a reference; waiting on the admin to confirm it. */
    PENDING,
    /** The admin confirmed the reservation - a confirmation email goes out at this point. */
    CONFIRMED
}
