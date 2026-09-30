package com.agencyvoyage.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.application.port.out.event.GroupBookingFinalizedEvent;
import com.agencyvoyage.application.port.out.event.ParticipantJoinedEvent;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;
import com.agencyvoyage.domain.booking.ParticipantId;
import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.infrastructure.config.AbstractKafkaIT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

class KafkaGroupBookingEventPublisherIT extends AbstractKafkaIT {

    @Autowired
    private KafkaGroupBookingEventPublisher publisher;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private KafkaConsumer<String, String> rawConsumer;

    @BeforeEach
    void setUp() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-raw-consumer-" + System.nanoTime());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        rawConsumer = new KafkaConsumer<>(props);
        rawConsumer.subscribe(java.util.List.of(KafkaTopics.PARTICIPANT_JOINED, KafkaTopics.FINALIZED));
    }

    @AfterEach
    void tearDown() {
        rawConsumer.close();
    }

    @Test
    void publishesParticipantJoinedAsJsonKeyedByBookingId() throws Exception {
        GroupBookingId bookingId = GroupBookingId.newId();
        ParticipantJoinedEvent event = new ParticipantJoinedEvent(
                bookingId,
                TripId.newId(),
                ParticipantId.newId(),
                "Alice",
                2,
                new BigDecimal("800.00"),
                Instant.parse("2027-01-01T00:00:00Z"));

        publisher.publishParticipantJoined(event);

        ConsumerRecord<String, String> record = pollUntilRecordOnTopic(KafkaTopics.PARTICIPANT_JOINED);
        assertThat(record.key()).isEqualTo(bookingId.toString());

        Map<?, ?> payload = objectMapper.readValue(record.value(), Map.class);
        assertThat(payload.get("bookingId")).isEqualTo(bookingId.toString());
        assertThat(payload.get("customerName")).isEqualTo("Alice");
        assertThat(payload.get("participantCount")).isEqualTo(2);
    }

    @Test
    void publishesFinalizedAsJsonKeyedByBookingId() throws Exception {
        GroupBookingId bookingId = GroupBookingId.newId();
        GroupBookingFinalizedEvent event = new GroupBookingFinalizedEvent(
                bookingId,
                TripId.newId(),
                GroupBookingStatus.CONFIRMED,
                3,
                new BigDecimal("600.00"),
                Instant.parse("2027-01-01T00:00:00Z"));

        publisher.publishFinalized(event);

        ConsumerRecord<String, String> record = pollUntilRecordOnTopic(KafkaTopics.FINALIZED);
        assertThat(record.key()).isEqualTo(bookingId.toString());

        Map<?, ?> payload = objectMapper.readValue(record.value(), Map.class);
        assertThat(payload.get("bookingId")).isEqualTo(bookingId.toString());
        assertThat(payload.get("status")).isEqualTo("CONFIRMED");
    }

    private ConsumerRecord<String, String> pollUntilRecordOnTopic(String topic) {
        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = rawConsumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                if (record.topic().equals(topic)) {
                    return record;
                }
            }
        }
        throw new AssertionError("No record observed on topic " + topic + " within the deadline");
    }
}
