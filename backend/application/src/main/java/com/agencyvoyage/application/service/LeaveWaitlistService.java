package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.application.port.in.LeaveWaitlistCommand;
import com.agencyvoyage.application.port.in.LeaveWaitlistUseCase;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.domain.booking.GroupBooking;
import java.util.Objects;

public final class LeaveWaitlistService implements LeaveWaitlistUseCase {

    private final GroupBookingRepository groupBookingRepository;

    public LeaveWaitlistService(GroupBookingRepository groupBookingRepository) {
        this.groupBookingRepository =
                Objects.requireNonNull(groupBookingRepository, "groupBookingRepository must not be null");
    }

    @Override
    public GroupBooking leaveWaitlist(LeaveWaitlistCommand command) {
        GroupBooking booking = groupBookingRepository.findById(command.bookingId())
                .orElseThrow(() -> new GroupBookingNotFoundException(command.bookingId()));

        booking.leaveWaitlist(command.userId());

        groupBookingRepository.save(booking);

        return booking;
    }
}
