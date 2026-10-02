package com.agencyvoyage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.agencyvoyage.application.port.in.SendContactMessageCommand;
import com.agencyvoyage.application.port.out.ContactMessageRepository;
import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendContactMessageServiceTest {

    private static final Instant NOW = Instant.parse("2027-01-01T00:00:00Z");

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @Captor
    private ArgumentCaptor<ContactMessage> messageCaptor;

    private SendContactMessageService service;

    @BeforeEach
    void setUp() {
        service = new SendContactMessageService(contactMessageRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void savesAMessageAuthoredByTheCallerAsTheStartOfTheirOwnConversation() {
        User alice = new User(UserId.newId(), "alice@example.com", "Alice");

        ContactMessage result = service.sendMessage(new SendContactMessageCommand(alice, "Where is my seat?"));

        verify(contactMessageRepository).save(messageCaptor.capture());
        ContactMessage saved = messageCaptor.getValue();
        assertThat(saved).isEqualTo(result);
        assertThat(saved.conversationUserId()).isEqualTo(alice.id());
        assertThat(saved.authorUserId()).isEqualTo(alice.id());
        assertThat(saved.authorName()).isEqualTo("Alice");
        assertThat(saved.authorEmail()).isEqualTo("alice@example.com");
        assertThat(saved.fromAdmin()).isFalse();
        assertThat(saved.message()).isEqualTo("Where is my seat?");
        assertThat(saved.sentAt()).isEqualTo(NOW);
    }
}
