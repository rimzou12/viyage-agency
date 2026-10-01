package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.application.port.in.RequestHotelReservationCommand;
import com.agencyvoyage.application.port.in.RequestHotelReservationUseCase;
import com.agencyvoyage.application.port.out.EmailSender;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.application.port.out.UserRepository;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.user.User;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class RequestHotelReservationService implements RequestHotelReservationUseCase {

    private final GroupBookingRepository groupBookingRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final Clock clock;

    public RequestHotelReservationService(
            GroupBookingRepository groupBookingRepository,
            UserRepository userRepository,
            EmailSender emailSender,
            Clock clock) {
        this.groupBookingRepository =
                Objects.requireNonNull(groupBookingRepository, "groupBookingRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.emailSender = Objects.requireNonNull(emailSender, "emailSender must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public GroupBooking requestHotelReservation(RequestHotelReservationCommand command) {
        GroupBooking booking = groupBookingRepository.findById(command.bookingId())
                .orElseThrow(() -> new GroupBookingNotFoundException(command.bookingId()));

        Instant now = clock.instant();
        booking.requestHotelReservation(command.reference(), now);

        groupBookingRepository.save(booking);

        booking.participants().forEach(participant -> userRepository
                .findById(participant.userId())
                .ifPresent(user -> sendRequestedEmail(user, booking)));

        return booking;
    }

    private void sendRequestedEmail(User user, GroupBooking booking) {
        emailSender.sendHotelReservationRequested(
                user.email(), user.displayName(), booking.id(), booking.hotelReservationReference());
    }
}
