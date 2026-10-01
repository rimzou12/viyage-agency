package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.application.port.in.RequestHotelReservationCommand;
import com.agencyvoyage.application.port.in.RequestHotelReservationUseCase;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.domain.booking.GroupBooking;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class RequestHotelReservationService implements RequestHotelReservationUseCase {

    private final GroupBookingRepository groupBookingRepository;
    private final Clock clock;

    public RequestHotelReservationService(GroupBookingRepository groupBookingRepository, Clock clock) {
        this.groupBookingRepository =
                Objects.requireNonNull(groupBookingRepository, "groupBookingRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public GroupBooking requestHotelReservation(RequestHotelReservationCommand command) {
        GroupBooking booking = groupBookingRepository.findById(command.bookingId())
                .orElseThrow(() -> new GroupBookingNotFoundException(command.bookingId()));

        Instant now = clock.instant();
        booking.requestHotelReservation(command.reference(), now);

        groupBookingRepository.save(booking);

        return booking;
    }
}
