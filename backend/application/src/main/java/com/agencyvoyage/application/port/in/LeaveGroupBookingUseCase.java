package com.agencyvoyage.application.port.in;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.exception.BookingClosedException;
import com.agencyvoyage.domain.exception.DeadlineExpiredException;
import com.agencyvoyage.domain.exception.ParticipantNotInBookingException;

public interface LeaveGroupBookingUseCase {

    /**
     * @throws GroupBookingNotFoundException if the booking does not exist
     * @throws ParticipantNotInBookingException if the participant isn't part of this booking
     * @throws BookingClosedException if the booking is no longer OPEN
     * @throws DeadlineExpiredException if the booking's deadline has passed
     */
    GroupBooking leaveGroupBooking(LeaveGroupBookingCommand command);
}
