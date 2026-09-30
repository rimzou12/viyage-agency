package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.booking.GroupBooking;

public interface LeaveWaitlistUseCase {

    GroupBooking leaveWaitlist(LeaveWaitlistCommand command);
}
