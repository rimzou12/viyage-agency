package com.agencyvoyage.domain.booking;

import java.time.Instant;
import java.util.Objects;

public record Participant(ParticipantId id, String customerName, Instant joinedAt) {

    public Participant {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(joinedAt, "joinedAt must not be null");
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("customerName must not be blank");
        }
    }
}
