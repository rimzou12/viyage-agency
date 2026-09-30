package com.agencyvoyage.web.dto;

import com.agencyvoyage.domain.booking.GroupBooking;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record GroupBookingResponse(
        String id,
        String tripId,
        String status,
        int participantCount,
        int minParticipants,
        int maxParticipants,
        BigDecimal currentPricePerSeat,
        Instant deadline,
        List<ParticipantResponse> participants) {

    public static GroupBookingResponse from(GroupBooking booking) {
        List<ParticipantResponse> participants =
                booking.participants().stream().map(ParticipantResponse::from).toList();
        return new GroupBookingResponse(
                booking.id().toString(),
                booking.tripId().toString(),
                booking.status().name(),
                booking.currentParticipantCount(),
                booking.minParticipants(),
                booking.maxParticipants(),
                booking.currentPricePerSeat(),
                booking.deadline(),
                participants);
    }
}
