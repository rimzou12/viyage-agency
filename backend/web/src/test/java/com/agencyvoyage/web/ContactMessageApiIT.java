package com.agencyvoyage.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.web.dto.AuthResponse;
import com.agencyvoyage.web.dto.ContactMessageResponse;
import com.agencyvoyage.web.dto.LoginRequest;
import com.agencyvoyage.web.dto.RegisterRequest;
import com.agencyvoyage.web.dto.ReplyToConversationRequest;
import com.agencyvoyage.web.dto.SendContactMessageRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies the threaded chat-with-admin flow end to end against the real REST API and
 * a real Testcontainers Postgres: a customer sends a message, the dev-seeded admin
 * replies into that customer's thread, both can read the conversation back, and a
 * regular user may not read someone else's thread or the admin-only flat inbox.
 */
class ContactMessageApiIT extends AbstractApiIT {

    private final RestTemplate rest = new RestTemplate();

    @Test
    void aCustomerAndTheAdminCanExchangeMessagesInAThread() {
        AuthResponse alice = registerAndLogin("Alice");
        String aliceToken = alice.token();
        String aliceId = alice.user().id();
        String adminToken = loginAsSeededAdmin();

        ContactMessageResponse sent = rest.exchange(
                        baseUrl() + "/api/contact-messages",
                        HttpMethod.POST,
                        authed(aliceToken, new SendContactMessageRequest("Where is my seat?")),
                        ContactMessageResponse.class)
                .getBody();
        assertThat(sent.authorName()).isEqualTo("Alice");
        assertThat(sent.fromAdmin()).isFalse();
        assertThat(sent.conversationUserId()).isEqualTo(aliceId);

        ContactMessageResponse reply = rest.exchange(
                        baseUrl() + "/api/contact-messages/reply",
                        HttpMethod.POST,
                        authed(adminToken, new ReplyToConversationRequest(aliceId, "Seat 12A")),
                        ContactMessageResponse.class)
                .getBody();
        assertThat(reply.fromAdmin()).isTrue();
        assertThat(reply.conversationUserId()).isEqualTo(aliceId);

        ContactMessageResponse[] aliceView = rest.exchange(
                        baseUrl() + "/api/contact-messages/conversations/" + aliceId,
                        HttpMethod.GET,
                        authed(aliceToken, null),
                        ContactMessageResponse[].class)
                .getBody();
        List<String> aliceMessages =
                List.of(aliceView).stream().map(ContactMessageResponse::message).toList();
        assertThat(aliceMessages).containsExactly("Where is my seat?", "Seat 12A");

        ContactMessageResponse[] adminView = rest.exchange(
                        baseUrl() + "/api/contact-messages/conversations/" + aliceId,
                        HttpMethod.GET,
                        authed(adminToken, null),
                        ContactMessageResponse[].class)
                .getBody();
        assertThat(adminView).hasSameSizeAs(aliceView);
    }

    @Test
    void requiresAuthenticationToSendAMessage() {
        try {
            rest.postForObject(
                    baseUrl() + "/api/contact-messages",
                    new SendContactMessageRequest("Where is my seat?"),
                    String.class);
            org.junit.jupiter.api.Assertions.fail("expected a 401");
        } catch (RestClientException e) {
            assertThat(e.getMessage()).contains("401");
        }
    }

    @Test
    void aRegularUserCannotReadSomeoneElsesConversation() {
        AuthResponse alice = registerAndLogin("Alice");
        rest.exchange(
                baseUrl() + "/api/contact-messages",
                HttpMethod.POST,
                authed(alice.token(), new SendContactMessageRequest("Where is my seat?")),
                ContactMessageResponse.class);

        AuthResponse bob = registerAndLogin("Bob");

        try {
            rest.exchange(
                    baseUrl() + "/api/contact-messages/conversations/" + alice.user().id(),
                    HttpMethod.GET,
                    authed(bob.token(), null),
                    ContactMessageResponse[].class);
            org.junit.jupiter.api.Assertions.fail("expected a 403, Bob doesn't own this conversation");
        } catch (RestClientException e) {
            assertThat(e.getMessage()).contains("403");
        }
    }

    @Test
    void aRegularUserCannotListTheFlatAdminInbox() {
        AuthResponse alice = registerAndLogin("Alice");

        try {
            rest.exchange(
                    baseUrl() + "/api/contact-messages",
                    HttpMethod.GET,
                    authed(alice.token(), null),
                    String.class);
            org.junit.jupiter.api.Assertions.fail("expected a 403, the caller is not an admin");
        } catch (RestClientException e) {
            assertThat(e.getMessage()).contains("403");
        }
    }

    @Test
    void aRegularUserCannotReplyAsTheAdmin() {
        AuthResponse alice = registerAndLogin("Alice");

        try {
            rest.exchange(
                    baseUrl() + "/api/contact-messages/reply",
                    HttpMethod.POST,
                    authed(alice.token(), new ReplyToConversationRequest(alice.user().id(), "Seat 12A")),
                    ContactMessageResponse.class);
            org.junit.jupiter.api.Assertions.fail("expected a 403, the caller is not an admin");
        } catch (RestClientException e) {
            assertThat(e.getMessage()).contains("403");
        }
    }

    private String loginAsSeededAdmin() {
        AuthResponse response = rest.postForObject(
                baseUrl() + "/api/auth/login",
                new LoginRequest("admin@agencyvoyage.example", "admin12345"),
                AuthResponse.class);
        return response.token();
    }

    private AuthResponse registerAndLogin(String displayName) {
        String email = displayName.toLowerCase() + "-" + UUID.randomUUID() + "@example.com";
        return rest.postForObject(
                baseUrl() + "/api/auth/register", new RegisterRequest(email, "password123", displayName), AuthResponse.class);
    }

    private <T> HttpEntity<T> authed(String token, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }
}
