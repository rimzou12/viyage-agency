package com.agencyvoyage.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.web.dto.AuthResponse;
import com.agencyvoyage.web.dto.GroupBookingResponse;
import com.agencyvoyage.web.dto.RegisterRequest;
import com.agencyvoyage.web.dto.TripResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class ReferralApiIT extends AbstractApiIT {

    private static final BigDecimal REFERRAL_DISCOUNT = new BigDecimal("50.00");

    private final RestTemplate rest = new RestTemplate();

    @Test
    void joiningViaAnInviteLinkDiscountsBothTheReferrerAndTheReferred() {
        TripResponse[] trips = rest.getForObject(baseUrl() + "/api/trips", TripResponse[].class);
        TripResponse trip = trips[0];

        String aliceToken = registerAndLogin("Alice");
        GroupBookingResponse created = post(baseUrl() + "/api/trips/" + trip.id() + "/group-bookings", aliceToken);
        String aliceParticipantId = created.myParticipantId();

        String bobToken = registerAndLogin("Bob");
        GroupBookingResponse afterReferredJoin = post(
                baseUrl() + "/api/group-bookings/" + created.id() + "/participants?ref=" + aliceParticipantId,
                bobToken);

        GroupBookingResponse aliceView = rest.exchange(
                        baseUrl() + "/api/group-bookings/" + created.id(),
                        HttpMethod.GET,
                        authed(aliceToken),
                        GroupBookingResponse.class)
                .getBody();
        GroupBookingResponse bobView = rest.exchange(
                        baseUrl() + "/api/group-bookings/" + created.id(),
                        HttpMethod.GET,
                        authed(bobToken),
                        GroupBookingResponse.class)
                .getBody();

        BigDecimal expectedPrice = afterReferredJoin.currentPricePerSeat().subtract(REFERRAL_DISCOUNT);
        assertThat(aliceView.myPricePerSeat()).isEqualByComparingTo(expectedPrice);
        assertThat(bobView.myPricePerSeat()).isEqualByComparingTo(expectedPrice);
    }

    @Test
    void anOrganicJoinPaysTheFullTierPriceWithNoDiscount() {
        TripResponse[] trips = rest.getForObject(baseUrl() + "/api/trips", TripResponse[].class);
        TripResponse trip = trips[0];

        GroupBookingResponse created =
                post(baseUrl() + "/api/trips/" + trip.id() + "/group-bookings", registerAndLogin("Alice"));
        String bobToken = registerAndLogin("Bob");
        GroupBookingResponse afterJoin =
                post(baseUrl() + "/api/group-bookings/" + created.id() + "/participants", bobToken);

        GroupBookingResponse bobView = rest.exchange(
                        baseUrl() + "/api/group-bookings/" + created.id(),
                        HttpMethod.GET,
                        authed(bobToken),
                        GroupBookingResponse.class)
                .getBody();

        assertThat(bobView.myPricePerSeat()).isEqualByComparingTo(afterJoin.currentPricePerSeat());
    }

    @Test
    void returns409WhenTheReferrerIsNotActuallyAParticipant() {
        TripResponse[] trips = rest.getForObject(baseUrl() + "/api/trips", TripResponse[].class);
        TripResponse trip = trips[0];

        GroupBookingResponse created =
                post(baseUrl() + "/api/trips/" + trip.id() + "/group-bookings", registerAndLogin("Alice"));

        try {
            post(
                    baseUrl() + "/api/group-bookings/" + created.id() + "/participants?ref=" + UUID.randomUUID(),
                    registerAndLogin("Bob"));
            org.junit.jupiter.api.Assertions.fail("expected a 409, the referrer isn't a participant");
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
}
