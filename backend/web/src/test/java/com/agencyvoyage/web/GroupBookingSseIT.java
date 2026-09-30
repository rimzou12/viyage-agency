package com.agencyvoyage.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.web.dto.CreateGroupBookingRequest;
import com.agencyvoyage.web.dto.GroupBookingResponse;
import com.agencyvoyage.web.dto.JoinGroupBookingRequest;
import com.agencyvoyage.web.dto.TripResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies the live-update wiring end to end: a real HTTP client opens the SSE stream,
 * a second HTTP call joins the group (through the real REST API, real Kafka producer,
 * real GroupBookingKafkaBridge consumer), and the SSE stream must carry the resulting
 * "participant-joined" event before the deadline.
 */
class GroupBookingSseIT extends AbstractApiIT {

    private final RestTemplate rest = new RestTemplate();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void sseStreamReceivesAPingWhenSomeoneJoinsTheBooking() throws Exception {
        TripResponse[] trips = rest.getForObject(baseUrl() + "/api/trips", TripResponse[].class);
        TripResponse trip = trips[0];

        GroupBookingResponse booking = rest.postForObject(
                baseUrl() + "/api/trips/" + trip.id() + "/group-bookings",
                new CreateGroupBookingRequest("Alice"),
                GroupBookingResponse.class);

        CompletableFuture<String> firstEventLine = openSseStreamAndCaptureFirstEventLine(booking.id());

        // give the SSE connection a moment to actually register before triggering the event
        Thread.sleep(500);

        rest.postForObject(
                baseUrl() + "/api/group-bookings/" + booking.id() + "/participants",
                new JoinGroupBookingRequest("Bob"),
                GroupBookingResponse.class);

        String eventLine = firstEventLine.get(15, TimeUnit.SECONDS);
        assertThat(eventLine).contains("participant-joined");
    }

    private CompletableFuture<String> openSseStreamAndCaptureFirstEventLine(String bookingId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/api/group-bookings/" + bookingId + "/events"))
                .GET()
                .build();

        return httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApplyAsync(response -> {
                    try (BufferedReader reader =
                            new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("event:")) {
                                return line;
                            }
                        }
                        throw new AssertionError("SSE stream closed before any event arrived");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
