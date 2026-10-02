package com.agencyvoyage.application.exception;

import com.agencyvoyage.domain.hotel.HotelId;

public final class HotelNotFoundException extends RuntimeException {

    public HotelNotFoundException(HotelId id) {
        super("No hotel found with id " + id);
    }
}
