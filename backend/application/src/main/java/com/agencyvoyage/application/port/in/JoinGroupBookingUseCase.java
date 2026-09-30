package com.agencyvoyage.application.port.in;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.exception.BookingClosedException;
import com.agencyvoyage.domain.exception.DeadlineExpiredException;
import com.agencyvoyage.domain.exception.GroupFullException;

public interface JoinGroupBookingUseCase {

    /**
     * @throws GroupBookingNotFoundException if the booking does not exist
     * @throws BookingClosedException if the booking is no longer OPEN
     * @throws DeadlineExpiredException if the booking's deadline has passed
     * @throws GroupFullException if the booking already has the maximum participants
     */
    GroupBooking joinGroupBooking(JoinGroupBookingCommand command);
}
