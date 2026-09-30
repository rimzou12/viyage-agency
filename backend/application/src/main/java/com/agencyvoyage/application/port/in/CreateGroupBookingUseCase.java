package com.agencyvoyage.application.port.in;

import com.agencyvoyage.application.exception.TripNotFoundException;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.exception.DeadlineExpiredException;

public interface CreateGroupBookingUseCase {

    /**
     * @throws TripNotFoundException if the trip does not exist
     * @throws DeadlineExpiredException if the trip's booking deadline has already passed
     */
    GroupBooking createGroupBooking(CreateGroupBookingCommand command);
}
