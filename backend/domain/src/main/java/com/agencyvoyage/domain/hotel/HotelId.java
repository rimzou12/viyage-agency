package com.agencyvoyage.domain.hotel;

import java.util.Objects;
import java.util.UUID;

public record HotelId(UUID value) {

    public HotelId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static HotelId newId() {
        return new HotelId(UUID.randomUUID());
    }

    public static HotelId of(String value) {
        return new HotelId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
