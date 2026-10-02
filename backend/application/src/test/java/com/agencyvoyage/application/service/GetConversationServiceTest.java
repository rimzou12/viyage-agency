package com.agencyvoyage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.agencyvoyage.application.exception.ConversationAccessDeniedException;
import com.agencyvoyage.application.port.in.GetConversationCommand;
import com.agencyvoyage.application.port.out.ContactMessageRepository;
import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.support.ContactMessageId;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetConversationServiceTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;

    private GetConversationService service;

    @BeforeEach
    void setUp() {
        service = new GetConversationService(contactMessageRepository);
    }

    @Test
    void letsACustomerViewTheirOwnConversation() {
        User alice = new User(UserId.newId(), "alice@example.com", "Alice");
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                alice.id(),
                alice.id(),
                "Alice",
                "alice@example.com",
                false,
                "Where is my seat?",
                Instant.parse("2027-01-01T00:00:00Z"));
        when(contactMessageRepository.findByConversationUserId(alice.id())).thenReturn(List.of(message));

        List<ContactMessage> conversation = service.getConversation(new GetConversationCommand(alice.id(), alice));

        assertThat(conversation).containsExactly(message);
    }

    @Test
    void letsAnAdminViewAnyConversation() {
        UserId customerId = UserId.newId();
        User admin = new User(UserId.newId(), "admin@example.com", "Admin", true);
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                customerId,
                customerId,
                "Alice",
                "alice@example.com",
                false,
                "Where is my seat?",
                Instant.parse("2027-01-01T00:00:00Z"));
        when(contactMessageRepository.findByConversationUserId(customerId)).thenReturn(List.of(message));

        List<ContactMessage> conversation =
                service.getConversation(new GetConversationCommand(customerId, admin));

        assertThat(conversation).containsExactly(message);
    }

    @Test
    void rejectsANonOwnerNonAdminCaller() {
        UserId customerId = UserId.newId();
        User bob = new User(UserId.newId(), "bob@example.com", "Bob");

        assertThatThrownBy(() -> service.getConversation(new GetConversationCommand(customerId, bob)))
                .isInstanceOf(ConversationAccessDeniedException.class);
    }
}
