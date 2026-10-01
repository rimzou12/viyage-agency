package com.agencyvoyage.domain.booking;

import com.agencyvoyage.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;

/**
 * @param referredBy the participant whose invite link this person joined through, or
 *                    {@code null} for an organic join (including the creator). Both
 *                    sides of a successful referral get a price break - see
 *                    {@link GroupBooking#pricePerSeatFor}.
 */
public record Participant(ParticipantId id, UserId userId, String customerName, Instant joinedAt, ParticipantId referredBy) {

    public Participant {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(joinedAt, "joinedAt must not be null");
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("customerName must not be blank");
        }
    }

    /** Convenience constructor for an organic join (no referrer). */
    public Participant(ParticipantId id, UserId userId, String customerName, Instant joinedAt) {
        this(id, userId, customerName, joinedAt, null);
    }
}
