package com.agencyvoyage.domain.trip;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A price break unlocked once a group reaches {@code minParticipants} members.
 */
public record PriceTier(int minParticipants, BigDecimal pricePerSeat) {

    public PriceTier {
        Objects.requireNonNull(pricePerSeat, "pricePerSeat must not be null");
        if (minParticipants < 2) {
            throw new IllegalArgumentException("minParticipants must be at least 2, got " + minParticipants);
        }
        if (pricePerSeat.signum() <= 0) {
            throw new IllegalArgumentException("pricePerSeat must be positive, got " + pricePerSeat);
        }
    }
}
