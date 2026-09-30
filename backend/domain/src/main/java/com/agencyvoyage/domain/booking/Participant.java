package com.agencyvoyage.domain.booking;

import com.agencyvoyage.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;

public record Participant(ParticipantId id, UserId userId, String customerName, Instant joinedAt) {

    public Participant {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(joinedAt, "joinedAt must not be null");
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("customerName must not be blank");
        }
    }
}
