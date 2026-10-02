package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.hotel.HotelId;
import com.agencyvoyage.domain.user.User;
import java.util.Objects;

public record DeleteHotelCommand(HotelId hotelId, User requestedBy) {

    public DeleteHotelCommand {
        Objects.requireNonNull(hotelId, "hotelId must not be null");
        Objects.requireNonNull(requestedBy, "requestedBy must not be null");
    }
}
