package com.agencyvoyage.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.infrastructure.messaging.kafka.KafkaTopics;
import com.agencyvoyage.web.dto.AuthResponse;
import com.agencyvoyage.web.dto.GroupBookingResponse;
import com.agencyvoyage.web.dto.RegisterRequest;
import com.agencyvoyage.web.dto.TripResponse;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class GroupBookingApiIT extends AbstractApiIT {

    private RestTemplate rest;
    private KafkaConsumer<String, String> rawConsumer;

    @BeforeEach
    void setUp() {
        rest = new RestTemplate();

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-raw-consumer-" + System.nanoTime());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        rawConsumer = new KafkaConsumer<>(props);
        rawConsumer.subscribe(List.of(KafkaTopics.PARTICIPANT_JOINED));
    }

    @AfterEach
    void tearDown() {
        rawConsumer.close();
    }

    @Test
    void groupBookingLifecycleDropsPricePerTierAndPersistsAcrossRequests() {
        TripResponse[] trips = rest.getForObject(baseUrl() + "/api/trips", TripResponse[].class);
        assertThat(trips).isNotEmpty();
        TripResponse trip = trips[0];

        GroupBookingResponse created = post(
                baseUrl() + "/api/trips/" + trip.id() + "/group-bookings", registerAndLogin("Alice"));
        assertThat(created.status()).isEqualTo("OPEN");
        assertThat(created.participantCount()).isEqualTo(1);
        assertThat(created.currentPricePerSeat()).isEqualByComparingTo(trip.basePrice());

        GroupBookingResponse afterOneMoreJoin = null;
        for (int i = 0; i < trip.priceTiers().get(0).minParticipants() - 1; i++) {
            afterOneMoreJoin = post(
                    baseUrl() + "/api/group-bookings/" + created.id() + "/participants",
                    registerAndLogin("Participant" + i));
        }
        assertThat(afterOneMoreJoin).isNotNull();
        assertThat(afterOneMoreJoin.participantCount()).isEqualTo(trip.priceTiers().get(0).minParticipants());
        assertThat(afterOneMoreJoin.currentPricePerSeat())
                .isEqualByComparingTo(trip.priceTiers().get(0).pricePerSeat());

        GroupBookingResponse reloaded =
                rest.getForObject(baseUrl() + "/api/group-bookings/" + created.id(), GroupBookingResponse.class);
        assertThat(reloaded.participantCount()).isEqualTo(afterOneMoreJoin.participantCount());
        assertThat(reloaded.currentPricePerSeat()).isEqualByComparingTo(afterOneMoreJoin.currentPricePerSeat());

        ConsumerRecord<String, String> event = pollUntilRecordForBooking(created.id());
        assertThat(event.value()).contains(created.id());
    }

    @Test
    void leavingRemovesTheParticipantAndDropsThePriceBackDown() {
        TripResponse[] trips = rest.getForObject(baseUrl() + "/api/trips", TripResponse[].class);
        TripResponse trip = trips[0];

        GroupBookingResponse created = post(
                baseUrl() + "/api/trips/" + trip.id() + "/group-bookings", registerAndLogin("Alice"));
        String bobToken = registerAndLogin("Bob");
        GroupBookingResponse afterBobJoined =
                post(baseUrl() + "/api/group-bookings/" + created.id() + "/participants", bobToken);
        assertThat(afterBobJoined.myParticipantId()).isNotNull();

        GroupBookingResponse afterBobLeft = rest.exchange(
                        baseUrl() + "/api/group-bookings/" + created.id() + "/participants/me",
                        HttpMethod.DELETE,
                        authed(bobToken),
                        GroupBookingResponse.class)
                .getBody();

        assertThat(afterBobLeft.participantCount()).isEqualTo(1);
        assertThat(afterBobLeft.currentPricePerSeat()).isEqualByComparingTo(trip.basePrice());

        GroupBookingResponse reloaded =
                rest.getForObject(baseUrl() + "/api/group-bookings/" + created.id(), GroupBookingResponse.class);
        assertThat(reloaded.participantCount()).isEqualTo(1);
    }

    @Test
    void returns404ForAnUnknownTrip() {
        try {
            post(baseUrl() + "/api/trips/" + UUID.randomUUID() + "/group-bookings", registerAndLogin("Alice"));
            org.junit.jupiter.api.Assertions.fail("expected a 404");
        } catch (RestClientException e) {
            assertThat(e.getMessage()).contains("404");
        }
    }

    @Test
    void returns401WithoutAToken() {
        try {
            rest.postForObject(
                    baseUrl() + "/api/trips/" + UUID.randomUUID() + "/group-bookings", null, String.class);
            org.junit.jupiter.api.Assertions.fail("expected a 401");
        } catch (RestClientException e) {
            assertThat(e.getMessage()).contains("401");
        }
    }

    @Test
    void returns409WhenTheGroupIsFull() {
        TripResponse[] trips = rest.getForObject(baseUrl() + "/api/trips", TripResponse[].class);
        TripResponse smallestGroupTrip = trips[0];
        for (TripResponse trip : trips) {
            if (trip.maxParticipants() < smallestGroupTrip.maxParticipants()) {
                smallestGroupTrip = trip;
            }
        }

        GroupBookingResponse booking = post(
                baseUrl() + "/api/trips/" + smallestGroupTrip.id() + "/group-bookings", registerAndLogin("Alice"));

        for (int i = booking.participantCount(); i < smallestGroupTrip.maxParticipants(); i++) {
            booking = post(
                    baseUrl() + "/api/group-bookings/" + booking.id() + "/participants",
                    registerAndLogin("Filler" + i));
        }

        try {
            post(
                    baseUrl() + "/api/group-bookings/" + booking.id() + "/participants",
                    registerAndLogin("OneTooMany"));
            org.junit.jupiter.api.Assertions.fail("expected a 409");
        } catch (RestClientException e) {
            assertThat(e.getMessage()).contains("409");
        }
    }

    private String registerAndLogin(String displayName) {
        String email = displayName.toLowerCase() + "-" + UUID.randomUUID() + "@example.com";
        AuthResponse response = rest.postForObject(
                baseUrl() + "/api/auth/register",
                new RegisterRequest(email, "password123", displayName),
                AuthResponse.class);
        return response.token();
    }

    private GroupBookingResponse post(String url, String token) {
        return rest.exchange(url, HttpMethod.POST, authed(token), GroupBookingResponse.class).getBody();
    }

    private HttpEntity<Void> authed(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    private ConsumerRecord<String, String> pollUntilRecordForBooking(String bookingId) {
        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = rawConsumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                if (bookingId.equals(record.key())) {
                    return record;
                }
            }
        }
        throw new AssertionError("No participant-joined event observed for booking " + bookingId);
    }
}
