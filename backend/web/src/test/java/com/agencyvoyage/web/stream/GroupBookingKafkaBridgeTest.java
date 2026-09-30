package com.agencyvoyage.web.stream;

import static org.mockito.Mockito.verify;

import com.agencyvoyage.domain.booking.GroupBookingStatus;
import com.agencyvoyage.infrastructure.messaging.kafka.dto.GroupBookingFinalizedMessage;
import com.agencyvoyage.infrastructure.messaging.kafka.dto.ParticipantJoinedMessage;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupBookingKafkaBridgeTest {

    @Mock
    private GroupBookingEventBroadcaster broadcaster;

    @Test
    void forwardsParticipantJoinedToTheBroadcaster() {
        GroupBookingKafkaBridge bridge = new GroupBookingKafkaBridge(broadcaster);
        ParticipantJoinedMessage message = new ParticipantJoinedMessage(
                "booking-1", "trip-1", "participant-1", "Alice", 1, new BigDecimal("1000"), Instant.now());

        bridge.onParticipantJoined(message);

        verify(broadcaster).broadcast("booking-1", "participant-joined");
    }

    @Test
    void forwardsFinalizedToTheBroadcaster() {
        GroupBookingKafkaBridge bridge = new GroupBookingKafkaBridge(broadcaster);
        GroupBookingFinalizedMessage message = new GroupBookingFinalizedMessage(
                "booking-1", "trip-1", GroupBookingStatus.CONFIRMED.name(), 5, new BigDecimal("800"), Instant.now());

        bridge.onFinalized(message);

        verify(broadcaster).broadcast("booking-1", "finalized");
    }
}
