package com.agencyvoyage.application.port.in;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.GroupBookingId;

public interface GetGroupBookingUseCase {

    /** @throws GroupBookingNotFoundException if the booking does not exist */
    GroupBooking getGroupBooking(GroupBookingId id);
}
