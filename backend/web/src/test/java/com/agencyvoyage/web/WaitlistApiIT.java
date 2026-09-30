package com.agencyvoyage.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.web.dto.AuthResponse;
import com.agencyvoyage.web.dto.GroupBookingResponse;
import com.agencyvoyage.web.dto.RegisterRequest;
import com.agencyvoyage.web.dto.TripResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class WaitlistApiIT extends AbstractApiIT {

    private final RestTemplate rest = new RestTemplate();

    @Test
    void joiningTheWaitlistOnAFullGroupThenLeavingPromotesTheWaitlistedUser() {
        TripResponse[] trips = rest.getForObject(baseUrl() + "/api/trips", TripResponse[].class);
        TripResponse smallestGroupTrip = trips[0];
        for (TripResponse trip : trips) {
            if (trip.maxParticipants() < smallestGroupTrip.maxParticipants()) {
                smallestGroupTrip = trip;
            }
        }

        String aliceToken = registerAndLogin("Alice");
        GroupBookingResponse booking =
                post(baseUrl() + "/api/trips/" + smallestGroupTrip.id() + "/group-bookings", aliceToken);
        for (int i = booking.participantCount(); i < smallestGroupTrip.maxParticipants(); i++) {
            booking = post(
                    baseUrl() + "/api/group-bookings/" + booking.id() + "/participants",
                    registerAndLogin("Filler" + i));
        }
        assertThat(booking.participantCount()).isEqualTo(smallestGroupTrip.maxParticipants());

        String bobToken = registerAndLogin("Bob");
        try {
            post(baseUrl() + "/api/group-bookings/" + booking.id() + "/participants", bobToken);
            org.junit.jupiter.api.Assertions.fail("expected a 409, the group is full");
        } catch (RestClientException e) {
            assertThat(e.getMessage()).contains("409");
        }

        GroupBookingResponse waitlisted =
                post(baseUrl() + "/api/group-bookings/" + booking.id() + "/waitlist", bobToken);
        assertThat(waitlisted.waitlist()).hasSize(1);
        assertThat(waitlisted.myWaitlistEntryId()).isNotNull();

        rest.exchange(
                baseUrl() + "/api/group-bookings/" + booking.id() + "/participants/me",
                HttpMethod.DELETE,
                authed(aliceToken),
                GroupBookingResponse.class);

        GroupBookingResponse afterPromotion = rest.getForObject(
                baseUrl() + "/api/group-bookings/" + booking.id(), GroupBookingResponse.class);
        assertThat(afterPromotion.waitlist()).isEmpty();
        assertThat(afterPromotion.participantCount()).isEqualTo(smallestGroupTrip.maxParticipants());
        assertThat(afterPromotion.participants())
                .extracting(p -> p.customerName())
                .contains("Bob");
    }

    @Test
    void cannotJoinTheWaitlistOfABookingThatStillHasRoom() {
        TripResponse[] trips = rest.getForObject(baseUrl() + "/api/trips", TripResponse[].class);
        TripResponse trip = trips[0];

        GroupBookingResponse booking =
                post(baseUrl() + "/api/trips/" + trip.id() + "/group-bookings", registerAndLogin("Alice"));

        try {
            post(baseUrl() + "/api/group-bookings/" + booking.id() + "/waitlist", registerAndLogin("Bob"));
            org.junit.jupiter.api.Assertions.fail("expected a 409, the group is not full yet");
        } catch (RestClientException e) {
            assertThat(e.getMessage()).contains("409");
        }
    }

    @Test
    void leavingTheWaitlistRemovesTheEntryWithoutTakingASeat() {
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

        String bobToken = registerAndLogin("Bob");
        post(baseUrl() + "/api/group-bookings/" + booking.id() + "/waitlist", bobToken);

        rest.exchange(
                baseUrl() + "/api/group-bookings/" + booking.id() + "/waitlist/me",
                HttpMethod.DELETE,
                authed(bobToken),
                GroupBookingResponse.class);

        GroupBookingResponse reloaded = rest.getForObject(
                baseUrl() + "/api/group-bookings/" + booking.id(), GroupBookingResponse.class);
        assertThat(reloaded.waitlist()).isEmpty();
        assertThat(reloaded.participantCount()).isEqualTo(smallestGroupTrip.maxParticipants());
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
}
