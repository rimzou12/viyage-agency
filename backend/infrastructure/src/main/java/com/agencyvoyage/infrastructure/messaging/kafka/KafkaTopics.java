package com.agencyvoyage.infrastructure.messaging.kafka;

public final class KafkaTopics {

    public static final String PARTICIPANT_JOINED = "group-booking.participant-joined";
    public static final String PARTICIPANT_LEFT = "group-booking.participant-left";
    public static final String FINALIZED = "group-booking.finalized";

    private KafkaTopics() {
    }
}
