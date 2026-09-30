package com.agencyvoyage.web.dto;

import com.agencyvoyage.domain.booking.Participant;
import java.time.Instant;

public record ParticipantResponse(String id, String customerName, Instant joinedAt) {

    public static ParticipantResponse from(Participant participant) {
        return new ParticipantResponse(
                participant.id().toString(), participant.customerName(), participant.joinedAt());
    }
}
