package com.agencyvoyage.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import com.agencyvoyage.application.port.in.GetConversationCommand;
import com.agencyvoyage.application.port.in.GetConversationUseCase;
import com.agencyvoyage.application.port.in.ListContactMessagesUseCase;
import com.agencyvoyage.application.port.in.ReplyToConversationCommand;
import com.agencyvoyage.application.port.in.ReplyToConversationUseCase;
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
    private static final User ADMIN = new User(UserId.newId(), "admin@example.com", "Admin", true);

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private SendContactMessageUseCase sendContactMessageUseCase;

    @MockitoBean
    private ReplyToConversationUseCase replyToConversationUseCase;

    @MockitoBean
    private GetConversationUseCase getConversationUseCase;

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
                ALICE.id(),
                ALICE.displayName(),
                ALICE.email(),
                false,
                "Where is my seat?",
                Instant.now());
        when(sendContactMessageUseCase.sendMessage(any(SendContactMessageCommand.class))).thenReturn(message);

        assertThat(mvc.post()
                        .uri("/api/contact-messages")
                        .with(asUser(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Where is my seat?\"}"))
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Where is my seat?");

        verify(sendContactMessageUseCase).sendMessage(new SendContactMessageCommand(ALICE, "Where is my seat?"));
    }

    @Test
    void sendMessageRequiresAuthentication() {
        assertThat(mvc.post()
                        .uri("/api/contact-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Where is my seat?\"}"))
                .hasStatus(401);
    }

    @Test
    void replyReturns201WithTheStoredReplyAsJson() {
        ContactMessage reply = new ContactMessage(
                ContactMessageId.newId(),
                ALICE.id(),
                ADMIN.id(),
                ADMIN.displayName(),
                ADMIN.email(),
                true,
                "Seat 12A",
                Instant.now());
        when(replyToConversationUseCase.reply(any(ReplyToConversationCommand.class))).thenReturn(reply);

        assertThat(mvc.post()
                        .uri("/api/contact-messages/reply")
                        .with(asUser(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conversationUserId\":\"" + ALICE.id() + "\",\"message\":\"Seat 12A\"}"))
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.fromAdmin")
                .isEqualTo(true);

        verify(replyToConversationUseCase)
                .reply(new ReplyToConversationCommand(ALICE.id(), ADMIN, "Seat 12A"));
    }

    @Test
    void getConversationReturnsTheThreadAsJson() {
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                ALICE.id(),
                ALICE.id(),
                ALICE.displayName(),
                ALICE.email(),
                false,
                "Where is my seat?",
                Instant.now());
        when(getConversationUseCase.getConversation(any(GetConversationCommand.class)))
                .thenReturn(List.of(message));

        assertThat(mvc.get()
                        .uri("/api/contact-messages/conversations/" + ALICE.id())
                        .with(asUser(ALICE)))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].message")
                .isEqualTo("Where is my seat?");
    }

    @Test
    void listMessagesReturnsEveryStoredMessageAsJson() {
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                ALICE.id(),
                ALICE.id(),
                ALICE.displayName(),
                ALICE.email(),
                false,
                "Where is my seat?",
                Instant.now());
        when(listContactMessagesUseCase.listMessages(ADMIN)).thenReturn(List.of(message));

        assertThat(mvc.get().uri("/api/contact-messages").with(asUser(ADMIN)))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].message")
                .isEqualTo("Where is my seat?");
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor asUser(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
        return authentication(authentication);
    }
}
