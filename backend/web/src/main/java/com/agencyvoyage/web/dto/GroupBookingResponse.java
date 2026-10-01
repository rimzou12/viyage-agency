package com.agencyvoyage.web.dto;

import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.Participant;
import com.agencyvoyage.domain.booking.ParticipantId;
import com.agencyvoyage.domain.booking.WaitlistEntry;
import com.agencyvoyage.domain.user.UserId;
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
        String myParticipantId,
        List<WaitlistEntryResponse> waitlist,
        String myWaitlistEntryId,
        BigDecimal myPricePerSeat,
        String hotelReservationStatus,
        String hotelReservationReference) {

    public static GroupBookingResponse from(GroupBooking booking) {
        return from(booking, null);
    }

    /**
     * @param currentUserId the caller's user id, if authenticated - used to find which
     *                      participant (if any) in this booking is theirs. Works the
     *                      same on every request, not just create/join, so "which one
     *                      is me" survives a page reload without any client-side state.
     */
    public static GroupBookingResponse from(GroupBooking booking, UserId currentUserId) {
        List<ParticipantResponse> participants =
                booking.participants().stream().map(ParticipantResponse::from).toList();
        List<WaitlistEntryResponse> waitlist =
                booking.waitlist().stream().map(WaitlistEntryResponse::from).toList();
        List<PriceTierResponse> priceTiers = booking.pricingSchedule().tiers().stream()
                .map(tier -> new PriceTierResponse(tier.minParticipants(), tier.pricePerSeat()))
                .toList();
        ParticipantId myParticipantId = currentUserId == null
                ? null
                : booking.participants().stream()
                        .filter(p -> p.userId().equals(currentUserId))
                        .map(Participant::id)
                        .findFirst()
                        .orElse(null);
        String myWaitlistEntryId = currentUserId == null
                ? null
                : booking.waitlist().stream()
                        .filter(w -> w.userId().equals(currentUserId))
                        .map(WaitlistEntry::id)
                        .map(Object::toString)
                        .findFirst()
                        .orElse(null);
        BigDecimal myPricePerSeat = myParticipantId == null ? null : booking.pricePerSeatFor(myParticipantId);
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
                myParticipantId == null ? null : myParticipantId.toString(),
                waitlist,
                myWaitlistEntryId,
                myPricePerSeat,
                booking.hotelReservationStatus().name(),
                booking.hotelReservationReference());
    }
}
