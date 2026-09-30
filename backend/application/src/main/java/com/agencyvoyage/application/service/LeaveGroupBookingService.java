package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.application.port.in.LeaveGroupBookingCommand;
import com.agencyvoyage.application.port.in.LeaveGroupBookingUseCase;
import com.agencyvoyage.application.port.out.GroupBookingEventPublisher;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.application.port.out.event.ParticipantLeftEvent;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.Participant;
import com.agencyvoyage.domain.exception.ParticipantNotInBookingException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

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

        booking.leave(command.userId(), now);

        groupBookingRepository.save(booking);

        eventPublisher.publishParticipantLeft(new ParticipantLeftEvent(
                booking.id(),
                booking.tripId(),
                departing.id(),
                booking.currentParticipantCount(),
                booking.currentPricePerSeat(),
                now));

        return booking;
    }
}
