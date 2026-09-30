package com.agencyvoyage.application.port.out;

import com.agencyvoyage.application.port.out.event.GroupBookingFinalizedEvent;
import com.agencyvoyage.application.port.out.event.ParticipantJoinedEvent;

public interface GroupBookingEventPublisher {

    void publishParticipantJoined(ParticipantJoinedEvent event);

    void publishFinalized(GroupBookingFinalizedEvent event);
}
