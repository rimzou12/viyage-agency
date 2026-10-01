package com.agencyvoyage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.agencyvoyage.application.port.out.ContactMessageRepository;
import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.support.ContactMessageId;
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
    void returnsWhateverTheRepositoryHas() {
        ListContactMessagesService service = new ListContactMessagesService(contactMessageRepository);
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                UserId.newId(),
                "Alice",
                "alice@example.com",
                "Help",
                "Where is my seat?",
                Instant.parse("2027-01-01T00:00:00Z"));
        when(contactMessageRepository.findAll()).thenReturn(List.of(message));

        assertThat(service.listMessages()).containsExactly(message);
    }
}
