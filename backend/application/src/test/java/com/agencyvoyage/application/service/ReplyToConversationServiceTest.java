package com.agencyvoyage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.agencyvoyage.application.exception.NotAnAdminException;
import com.agencyvoyage.application.port.in.ReplyToConversationCommand;
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
class ReplyToConversationServiceTest {

    private static final Instant NOW = Instant.parse("2027-01-01T00:00:00Z");

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @Captor
    private ArgumentCaptor<ContactMessage> messageCaptor;

    private ReplyToConversationService service;

    @BeforeEach
    void setUp() {
        service = new ReplyToConversationService(contactMessageRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void savesAReplyAuthoredByTheAdminIntoTheCustomersConversation() {
        UserId customerId = UserId.newId();
        User admin = new User(UserId.newId(), "admin@example.com", "Admin", true);

        ContactMessage result =
                service.reply(new ReplyToConversationCommand(customerId, admin, "Seat 12A, enjoy your trip!"));

        verify(contactMessageRepository).save(messageCaptor.capture());
        ContactMessage saved = messageCaptor.getValue();
        assertThat(saved).isEqualTo(result);
        assertThat(saved.conversationUserId()).isEqualTo(customerId);
        assertThat(saved.authorUserId()).isEqualTo(admin.id());
        assertThat(saved.authorName()).isEqualTo("Admin");
        assertThat(saved.authorEmail()).isEqualTo("admin@example.com");
        assertThat(saved.fromAdmin()).isTrue();
        assertThat(saved.message()).isEqualTo("Seat 12A, enjoy your trip!");
        assertThat(saved.sentAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsANonAdminCaller() {
        User regularUser = new User(UserId.newId(), "alice@example.com", "Alice");

        assertThatThrownBy(() -> service.reply(new ReplyToConversationCommand(UserId.newId(), regularUser, "Hi")))
                .isInstanceOf(NotAnAdminException.class);
    }
}
