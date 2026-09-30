package com.agencyvoyage.domain.trip;

import java.util.Objects;
import java.util.UUID;

public record TripId(UUID value) {

    public TripId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static TripId newId() {
        return new TripId(UUID.randomUUID());
    }

    public static TripId of(String value) {
        return new TripId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
