package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.application.port.in.LeaveGroupBookingCommand;
import com.agencyvoyage.application.port.in.LeaveGroupBookingUseCase;
import com.agencyvoyage.application.port.out.GroupBookingEventPublisher;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.application.port.out.event.ParticipantJoinedEvent;
import com.agencyvoyage.application.port.out.event.ParticipantLeftEvent;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.Participant;
import com.agencyvoyage.domain.exception.ParticipantNotInBookingException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class LeaveGroupBookingService implements LeaveGroupBookingUseCase {

    private final GroupBookingRepository groupBookingRepository;
    private final GroupBookingEventPublisher eventPublisher;
    private final Clock clock;

    public LeaveGroupBookingService(
            GroupBookingRepository groupBookingRepository, GroupBookingEventPublisher eventPublisher, Clock clock) {
        this.groupBookingRepository =
                Objects.requireNonNull(groupBookingRepository, "groupBookingRepository must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public GroupBooking leaveGroupBooking(LeaveGroupBookingCommand command) {
        GroupBooking booking = groupBookingRepository.findById(command.bookingId())
                .orElseThrow(() -> new GroupBookingNotFoundException(command.bookingId()));

        Instant now = clock.instant();
        Participant departing = booking.participants().stream()
                .filter(p -> p.userId().equals(command.userId()))
                .findFirst()
                .orElseThrow(() -> new ParticipantNotInBookingException(booking.id(), command.userId()));

        Optional<Participant> promoted = booking.leave(command.userId(), now);

        groupBookingRepository.save(booking);

        eventPublisher.publishParticipantLeft(new ParticipantLeftEvent(
                booking.id(),
                booking.tripId(),
                departing.id(),
                booking.currentParticipantCount(),
                booking.currentPricePerSeat(),
                now));

        // Reuses the regular join event: from every consumer's point of view (notifications,
        // audit trail, the SSE bridge) a waitlist promotion is indistinguishable from an
        // ordinary join - it fills the same seat the same way, just triggered by a departure
        // instead of a fresh request.
        promoted.ifPresent(newParticipant -> eventPublisher.publishParticipantJoined(new ParticipantJoinedEvent(
                booking.id(),
                booking.tripId(),
                newParticipant.id(),
                newParticipant.customerName(),
                booking.currentParticipantCount(),
                booking.currentPricePerSeat(),
                now)));

        return booking;
    }
}
