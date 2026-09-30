package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.booking.GroupBooking;

public interface JoinWaitlistUseCase {

    GroupBooking joinWaitlist(JoinWaitlistCommand command);
}
