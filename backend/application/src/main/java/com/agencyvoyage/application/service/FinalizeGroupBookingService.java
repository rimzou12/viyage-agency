package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.application.port.in.FinalizeGroupBookingUseCase;
import com.agencyvoyage.application.port.out.GroupBookingEventPublisher;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.application.port.out.event.GroupBookingFinalizedEvent;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class FinalizeGroupBookingService implements FinalizeGroupBookingUseCase {

    private final GroupBookingRepository groupBookingRepository;
    private final GroupBookingEventPublisher eventPublisher;
    private final Clock clock;

    public FinalizeGroupBookingService(
            GroupBookingRepository groupBookingRepository, GroupBookingEventPublisher eventPublisher, Clock clock) {
        this.groupBookingRepository =
                Objects.requireNonNull(groupBookingRepository, "groupBookingRepository must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public GroupBookingStatus finalizeGroupBooking(GroupBookingId id) {
        GroupBooking booking = groupBookingRepository.findById(id)
                .orElseThrow(() -> new GroupBookingNotFoundException(id));

        return doFinalize(booking, clock.instant());
    }

    @Override
    public List<GroupBookingId> finalizeAllDue(Instant now) {
        List<GroupBooking> due = groupBookingRepository.findOpenWithDeadlineAtOrBefore(now);
        return due.stream().map(booking -> {
            doFinalize(booking, now);
            return booking.id();
        }).toList();
    }

    private GroupBookingStatus doFinalize(GroupBooking booking, Instant now) {
        GroupBookingStatus status = booking.finalizeBooking(now);
        groupBookingRepository.save(booking);

        eventPublisher.publishFinalized(new GroupBookingFinalizedEvent(
                booking.id(),
                booking.tripId(),
                status,
                booking.currentParticipantCount(),
                booking.currentPricePerSeat(),
                now));

        return status;
    }
}
