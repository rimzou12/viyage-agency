package com.agencyvoyage.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.web.dto.AuditEventResponse;
import com.agencyvoyage.web.dto.AuthResponse;
import com.agencyvoyage.web.dto.GroupBookingResponse;
import com.agencyvoyage.web.dto.RegisterRequest;
import com.agencyvoyage.web.dto.TripResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies the audit trail end to end: the real REST API, real Kafka producer, and the
 * real {@code AuditTrailKafkaListener} consumer persisting to real Testcontainers
 * Postgres. Unlike the rest of the booking state (written synchronously inside the
 * use case), the audit trail is only queryable once its consumer has processed the
 * event, so the assertions poll the endpoint rather than expecting it immediately.
 */
class AuditTrailApiIT extends AbstractApiIT {

    private final RestTemplate rest = new RestTemplate();

    @Test
    void recordsJoinLeaveAndFinalizationInChronologicalOrder() {
        TripResponse[] trips = rest.getForObject(baseUrl() + "/api/trips", TripResponse[].class);
        TripResponse trip = trips[0];

        GroupBookingResponse created = post(
                baseUrl() + "/api/trips/" + trip.id() + "/group-bookings", registerAndLogin("Alice"));
        String bobToken = registerAndLogin("Bob");
        post(baseUrl() + "/api/group-bookings/" + created.id() + "/participants", bobToken);
        rest.exchange(
                baseUrl() + "/api/group-bookings/" + created.id() + "/participants/me",
                HttpMethod.DELETE,
                authed(bobToken),
                GroupBookingResponse.class);

        List<AuditEventResponse> history = pollUntilAtLeast(created.id(), 3);

        assertThat(history).extracting(AuditEventResponse::type)
                .containsExactly("PARTICIPANT_JOINED", "PARTICIPANT_JOINED", "PARTICIPANT_LEFT");
        assertThat(history.get(0).customerName()).isEqualTo("Alice");
        assertThat(history.get(1).customerName()).isEqualTo("Bob");
        assertThat(history).isSortedAccordingTo((a, b) -> a.occurredAt().compareTo(b.occurredAt()));
    }

    @Test
    void returnsAnEmptyListForABookingWithNoRecordedHistoryYet() {
        AuditEventResponse[] history = rest.getForObject(
                baseUrl() + "/api/group-bookings/" + UUID.randomUUID() + "/audit-trail", AuditEventResponse[].class);

        assertThat(history).isEmpty();
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

    private List<AuditEventResponse> pollUntilAtLeast(String bookingId, int expectedSize) {
        long deadline = System.currentTimeMillis() + 15_000;
        List<AuditEventResponse> last = List.of();
        while (System.currentTimeMillis() < deadline) {
            AuditEventResponse[] history = rest.getForObject(
                    baseUrl() + "/api/group-bookings/" + bookingId + "/audit-trail", AuditEventResponse[].class);
            last = List.of(history);
            if (last.size() >= expectedSize) {
                return last;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        }
        throw new AssertionError(
                "Audit trail for booking " + bookingId + " never reached " + expectedSize + " entries; last seen: " + last);
    }
}
