package com.agencyvoyage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.agencyvoyage.application.exception.NotAnAdminException;
import com.agencyvoyage.application.port.out.ContactMessageRepository;
import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.support.ContactMessageId;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListContactMessagesServiceTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @Test
    void returnsWhateverTheRepositoryHasWhenCalledByAnAdmin() {
        ListContactMessagesService service = new ListContactMessagesService(contactMessageRepository);
        UserId customerId = UserId.newId();
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                customerId,
                customerId,
                "Alice",
                "alice@example.com",
                false,
                "Where is my seat?",
                Instant.parse("2027-01-01T00:00:00Z"));
        when(contactMessageRepository.findAll()).thenReturn(List.of(message));

        User admin = new User(UserId.newId(), "admin@example.com", "Admin", true);

        assertThat(service.listMessages(admin)).containsExactly(message);
    }

    @Test
    void rejectsANonAdminCaller() {
        ListContactMessagesService service = new ListContactMessagesService(contactMessageRepository);
        User regularUser = new User(UserId.newId(), "alice@example.com", "Alice");

        assertThatThrownBy(() -> service.listMessages(regularUser)).isInstanceOf(NotAnAdminException.class);
    }
}
