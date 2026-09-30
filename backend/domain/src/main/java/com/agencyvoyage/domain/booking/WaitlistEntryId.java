package com.agencyvoyage.domain.booking;

import java.util.Objects;
import java.util.UUID;

public record WaitlistEntryId(UUID value) {

    public WaitlistEntryId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static WaitlistEntryId newId() {
        return new WaitlistEntryId(UUID.randomUUID());
    }

    public static WaitlistEntryId of(String value) {
        return new WaitlistEntryId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
