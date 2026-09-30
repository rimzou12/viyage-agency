package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.application.port.in.JoinGroupBookingCommand;
import com.agencyvoyage.application.port.in.JoinGroupBookingUseCase;
import com.agencyvoyage.application.port.out.GroupBookingEventPublisher;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.application.port.out.event.ParticipantJoinedEvent;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.Participant;
import com.agencyvoyage.domain.booking.ParticipantId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class JoinGroupBookingService implements JoinGroupBookingUseCase {

    private final GroupBookingRepository groupBookingRepository;
    private final GroupBookingEventPublisher eventPublisher;
    private final Clock clock;

    public JoinGroupBookingService(
            GroupBookingRepository groupBookingRepository, GroupBookingEventPublisher eventPublisher, Clock clock) {
        this.groupBookingRepository =
                Objects.requireNonNull(groupBookingRepository, "groupBookingRepository must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public GroupBooking joinGroupBooking(JoinGroupBookingCommand command) {
        GroupBooking booking = groupBookingRepository.findById(command.bookingId())
                .orElseThrow(() -> new GroupBookingNotFoundException(command.bookingId()));

        Instant now = clock.instant();
        Participant participant = new Participant(
                ParticipantId.newId(), command.actingUser().id(), command.actingUser().displayName(), now);
        booking.join(participant, now);

        groupBookingRepository.save(booking);

        eventPublisher.publishParticipantJoined(new ParticipantJoinedEvent(
                booking.id(),
                booking.tripId(),
                participant.id(),
                participant.customerName(),
                booking.currentParticipantCount(),
                booking.currentPricePerSeat(),
                now));

        return booking;
    }
}
