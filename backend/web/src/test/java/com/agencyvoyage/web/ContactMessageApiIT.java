package com.agencyvoyage.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.web.dto.AuthResponse;
import com.agencyvoyage.web.dto.ContactMessageResponse;
import com.agencyvoyage.web.dto.RegisterRequest;
import com.agencyvoyage.web.dto.SendContactMessageRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies the "contact the admin" flow end to end against the real REST API and a
 * real Testcontainers Postgres. There is no dedicated admin role yet, so any logged-in
 * user may list messages - that simplification is exercised here too.
 */
class ContactMessageApiIT extends AbstractApiIT {

    private final RestTemplate rest = new RestTemplate();

    @Test
    void sendsAndListsAMessageNewestFirst() {
        String aliceToken = registerAndLogin("Alice");

        ContactMessageResponse first = rest.exchange(
                        baseUrl() + "/api/contact-messages",
                        HttpMethod.POST,
                        authed(aliceToken, new SendContactMessageRequest("Help", "Where is my seat?")),
                        ContactMessageResponse.class)
                .getBody();
        ContactMessageResponse second = rest.exchange(
                        baseUrl() + "/api/contact-messages",
                        HttpMethod.POST,
                        authed(aliceToken, new SendContactMessageRequest("Refund", "Can I get a refund?")),
                        ContactMessageResponse.class)
                .getBody();

        assertThat(first.authorName()).isEqualTo("Alice");
        assertThat(first.subject()).isEqualTo("Help");

        ContactMessageResponse[] messages = rest.exchange(
                        baseUrl() + "/api/contact-messages",
                        HttpMethod.GET,
                        authed(aliceToken, null),
                        ContactMessageResponse[].class)
                .getBody();

        List<String> subjects = List.of(messages).stream().map(ContactMessageResponse::subject).toList();
        assertThat(subjects.indexOf(second.subject())).isLessThan(subjects.indexOf(first.subject()));
    }

    @Test
    void requiresAuthenticationToSendAMessage() {
        try {
            rest.postForObject(
                    baseUrl() + "/api/contact-messages",
                    new SendContactMessageRequest("Help", "Where is my seat?"),
                    String.class);
            org.junit.jupiter.api.Assertions.fail("expected a 401");
        } catch (org.springframework.web.client.RestClientException e) {
            assertThat(e.getMessage()).contains("401");
        }
    }

    private String registerAndLogin(String displayName) {
        String email = displayName.toLowerCase() + "-" + UUID.randomUUID() + "@example.com";
        AuthResponse response = rest.postForObject(
                baseUrl() + "/api/auth/register", new RegisterRequest(email, "password123", displayName), AuthResponse.class);
        return response.token();
    }

    private <T> HttpEntity<T> authed(String token, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }
}
