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
        List<PriceTierResponse> priceTiers,
        List<ParticipantResponse> participants,
        String myParticipantId) {

    public static GroupBookingResponse from(GroupBooking booking) {
        return from(booking, null);
    }

    /**
     * @param myParticipantId the id of the participant this specific request just
     *                        created/added, if any - lets the caller remember "which
     *                        one is me" without any auth. Null for a plain read.
     */
    public static GroupBookingResponse from(GroupBooking booking, String myParticipantId) {
        List<ParticipantResponse> participants =
                booking.participants().stream().map(ParticipantResponse::from).toList();
        List<PriceTierResponse> priceTiers = booking.pricingSchedule().tiers().stream()
                .map(tier -> new PriceTierResponse(tier.minParticipants(), tier.pricePerSeat()))
                .toList();
        return new GroupBookingResponse(
                booking.id().toString(),
                booking.tripId().toString(),
                booking.status().name(),
                booking.currentParticipantCount(),
                booking.minParticipants(),
                booking.maxParticipants(),
                booking.currentPricePerSeat(),
                booking.deadline(),
                priceTiers,
                participants,
                myParticipantId);
    }
}
