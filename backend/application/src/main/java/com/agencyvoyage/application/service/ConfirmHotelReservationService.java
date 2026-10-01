package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.application.port.in.ConfirmHotelReservationCommand;
import com.agencyvoyage.application.port.in.ConfirmHotelReservationUseCase;
import com.agencyvoyage.application.port.out.EmailSender;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.application.port.out.UserRepository;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.user.User;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class ConfirmHotelReservationService implements ConfirmHotelReservationUseCase {

    private final GroupBookingRepository groupBookingRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final Clock clock;

    public ConfirmHotelReservationService(
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
    public GroupBooking confirmHotelReservation(ConfirmHotelReservationCommand command) {
        GroupBooking booking = groupBookingRepository.findById(command.bookingId())
                .orElseThrow(() -> new GroupBookingNotFoundException(command.bookingId()));

        Instant now = clock.instant();
        booking.confirmHotelReservation(now);

        groupBookingRepository.save(booking);

        booking.participants().forEach(participant -> userRepository
                .findById(participant.userId())
                .ifPresent(user -> sendConfirmationEmail(user, booking)));

        return booking;
    }

    private void sendConfirmationEmail(User user, GroupBooking booking) {
        emailSender.sendHotelReservationConfirmed(
                user.email(), user.displayName(), booking.id(), booking.hotelReservationReference());
    }
}
