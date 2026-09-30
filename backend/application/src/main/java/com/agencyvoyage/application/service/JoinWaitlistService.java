package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.application.port.in.JoinWaitlistCommand;
import com.agencyvoyage.application.port.in.JoinWaitlistUseCase;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.WaitlistEntry;
import com.agencyvoyage.domain.booking.WaitlistEntryId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class JoinWaitlistService implements JoinWaitlistUseCase {

    private final GroupBookingRepository groupBookingRepository;
    private final Clock clock;

    public JoinWaitlistService(GroupBookingRepository groupBookingRepository, Clock clock) {
        this.groupBookingRepository =
                Objects.requireNonNull(groupBookingRepository, "groupBookingRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public GroupBooking joinWaitlist(JoinWaitlistCommand command) {
        GroupBooking booking = groupBookingRepository.findById(command.bookingId())
                .orElseThrow(() -> new GroupBookingNotFoundException(command.bookingId()));

        Instant now = clock.instant();
        WaitlistEntry entry = new WaitlistEntry(
                WaitlistEntryId.newId(), command.actingUser().id(), command.actingUser().displayName(), now);
        booking.joinWaitlist(entry, now);

        groupBookingRepository.save(booking);

        return booking;
    }
}
