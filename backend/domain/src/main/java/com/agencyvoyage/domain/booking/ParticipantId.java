package com.agencyvoyage.domain.booking;

import java.util.Objects;
import java.util.UUID;

public record ParticipantId(UUID value) {

    public ParticipantId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ParticipantId newId() {
        return new ParticipantId(UUID.randomUUID());
    }

    public static ParticipantId of(String value) {
        return new ParticipantId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
