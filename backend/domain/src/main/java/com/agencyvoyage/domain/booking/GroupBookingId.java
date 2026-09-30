package com.agencyvoyage.domain.booking;

import java.util.Objects;
import java.util.UUID;

public record GroupBookingId(UUID value) {

    public GroupBookingId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static GroupBookingId newId() {
        return new GroupBookingId(UUID.randomUUID());
    }

    public static GroupBookingId of(String value) {
        return new GroupBookingId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
