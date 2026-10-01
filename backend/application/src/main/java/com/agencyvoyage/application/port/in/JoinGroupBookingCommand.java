package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.ParticipantId;
import com.agencyvoyage.domain.user.User;
import java.util.Objects;

/**
 * @param referrerParticipantId the participant whose invite link the caller joined
 *                               through, or {@code null} for an organic join.
 */
public record JoinGroupBookingCommand(GroupBookingId bookingId, User actingUser, ParticipantId referrerParticipantId) {

    public JoinGroupBookingCommand {
        Objects.requireNonNull(bookingId, "bookingId must not be null");
        Objects.requireNonNull(actingUser, "actingUser must not be null");
    }

    /** Convenience constructor for an organic join (no referrer). */
    public JoinGroupBookingCommand(GroupBookingId bookingId, User actingUser) {
        this(bookingId, actingUser, null);
    }
}
