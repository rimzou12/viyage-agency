package com.agencyvoyage.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import com.agencyvoyage.application.port.in.ListContactMessagesUseCase;
import com.agencyvoyage.application.port.in.SendContactMessageCommand;
import com.agencyvoyage.application.port.in.SendContactMessageUseCase;
import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.support.ContactMessageId;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import com.agencyvoyage.infrastructure.security.JwtTokenParser;
import com.agencyvoyage.web.config.CorsConfig;
import com.agencyvoyage.web.security.SecurityConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ContactMessageController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class ContactMessageControllerTest {

    private static final User ALICE = new User(UserId.newId(), "alice@example.com", "Alice");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private SendContactMessageUseCase sendContactMessageUseCase;

    @MockitoBean
    private ListContactMessagesUseCase listContactMessagesUseCase;

    /**
     * Not used by ContactMessageController, but JwtAuthenticationFilter is a servlet
     * Filter, so @WebMvcTest's scanning constructs it regardless of which controller
     * is under test - it needs this dependency satisfied to build the context at all.
     */
    @MockitoBean
    private JwtTokenParser jwtTokenParser;

    @Test
    void sendMessageReturns201WithTheStoredMessageAsJson() {
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                ALICE.id(),
                ALICE.displayName(),
                ALICE.email(),
                "Help",
                "Where is my seat?",
                Instant.now());
        when(sendContactMessageUseCase.sendMessage(any(SendContactMessageCommand.class))).thenReturn(message);

        assertThat(mvc.post()
                        .uri("/api/contact-messages")
                        .with(asAlice())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"Help\",\"message\":\"Where is my seat?\"}"))
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.subject")
                .isEqualTo("Help");

        verify(sendContactMessageUseCase)
                .sendMessage(new SendContactMessageCommand(ALICE, "Help", "Where is my seat?"));
    }

    @Test
    void sendMessageRequiresAuthentication() {
        assertThat(mvc.post()
                        .uri("/api/contact-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"Help\",\"message\":\"Where is my seat?\"}"))
                .hasStatus(401);
    }

    @Test
    void listMessagesReturnsEveryStoredMessageAsJson() {
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                ALICE.id(),
                ALICE.displayName(),
                ALICE.email(),
                "Help",
                "Where is my seat?",
                Instant.now());
        when(listContactMessagesUseCase.listMessages()).thenReturn(List.of(message));

        assertThat(mvc.get().uri("/api/contact-messages").with(asAlice()))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].subject")
                .isEqualTo("Help");
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor asAlice() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(ALICE, null, List.of());
        return authentication(authentication);
    }
}
