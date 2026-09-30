package com.agencyvoyage.application.port.out;

import com.agencyvoyage.application.port.out.event.GroupBookingFinalizedEvent;
import com.agencyvoyage.application.port.out.event.ParticipantJoinedEvent;
import com.agencyvoyage.application.port.out.event.ParticipantLeftEvent;

public interface GroupBookingEventPublisher {

    void publishParticipantJoined(ParticipantJoinedEvent event);

    void publishParticipantLeft(ParticipantLeftEvent event);

    void publishFinalized(GroupBookingFinalizedEvent event);
}
