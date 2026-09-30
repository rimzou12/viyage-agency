package com.agencyvoyage.infrastructure.messaging.kafka;

import com.agencyvoyage.application.port.out.GroupBookingEventPublisher;
import com.agencyvoyage.application.port.out.event.GroupBookingFinalizedEvent;
import com.agencyvoyage.application.port.out.event.ParticipantJoinedEvent;
import com.agencyvoyage.infrastructure.messaging.kafka.dto.GroupBookingFinalizedMessage;
import com.agencyvoyage.infrastructure.messaging.kafka.dto.ParticipantJoinedMessage;
import java.util.Objects;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes group-booking domain events to Kafka. The booking id is used as the
 * record key so every event for one booking lands in the same partition, keeping
 * per-booking ordering (join before join, finalize after every join).
 */
@Component
public class KafkaGroupBookingEventPublisher implements GroupBookingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaGroupBookingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");
    }

    @Override
    public void publishParticipantJoined(ParticipantJoinedEvent event) {
        String key = event.bookingId().toString();
        ParticipantJoinedMessage message = new ParticipantJoinedMessage(
                event.bookingId().toString(),
                event.tripId().toString(),
                event.participantId().toString(),
                event.customerName(),
                event.participantCount(),
                event.pricePerSeat(),
                event.occurredAt());
        kafkaTemplate.send(KafkaTopics.PARTICIPANT_JOINED, key, message);
    }

    @Override
    public void publishFinalized(GroupBookingFinalizedEvent event) {
        String key = event.bookingId().toString();
        GroupBookingFinalizedMessage message = new GroupBookingFinalizedMessage(
                event.bookingId().toString(),
                event.tripId().toString(),
                event.status().name(),
                event.participantCount(),
                event.finalPricePerSeat(),
                event.occurredAt());
        kafkaTemplate.send(KafkaTopics.FINALIZED, key, message);
    }
}
