package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.ParticipantId;
import java.util.Objects;

public record LeaveGroupBookingCommand(GroupBookingId bookingId, ParticipantId participantId) {

    public LeaveGroupBookingCommand {
        Objects.requireNonNull(bookingId, "bookingId must not be null");
        Objects.requireNonNull(participantId, "participantId must not be null");
    }
}
