package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.TripNotFoundException;
import com.agencyvoyage.application.port.in.CreateGroupBookingCommand;
import com.agencyvoyage.application.port.in.CreateGroupBookingUseCase;
import com.agencyvoyage.application.port.out.GroupBookingEventPublisher;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.application.port.out.TripRepository;
import com.agencyvoyage.application.port.out.event.ParticipantJoinedEvent;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.Participant;
import com.agencyvoyage.domain.booking.ParticipantId;
import com.agencyvoyage.domain.trip.Trip;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class CreateGroupBookingService implements CreateGroupBookingUseCase {

    private final TripRepository tripRepository;
    private final GroupBookingRepository groupBookingRepository;
    private final GroupBookingEventPublisher eventPublisher;
    private final Clock clock;

    public CreateGroupBookingService(
            TripRepository tripRepository,
            GroupBookingRepository groupBookingRepository,
            GroupBookingEventPublisher eventPublisher,
            Clock clock) {
        this.tripRepository = Objects.requireNonNull(tripRepository, "tripRepository must not be null");
        this.groupBookingRepository =
                Objects.requireNonNull(groupBookingRepository, "groupBookingRepository must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public GroupBooking createGroupBooking(CreateGroupBookingCommand command) {
        Trip trip = tripRepository.findById(command.tripId())
                .orElseThrow(() -> new TripNotFoundException(command.tripId()));

        Instant now = clock.instant();
        Participant creator = new Participant(ParticipantId.newId(), command.customerName(), now);
        GroupBooking booking = GroupBooking.open(GroupBookingId.newId(), trip, creator, now);

        groupBookingRepository.save(booking);

        eventPublisher.publishParticipantJoined(new ParticipantJoinedEvent(
                booking.id(),
                booking.tripId(),
                creator.id(),
                creator.customerName(),
                booking.currentParticipantCount(),
                booking.currentPricePerSeat(),
                now));

        return booking;
    }
}
