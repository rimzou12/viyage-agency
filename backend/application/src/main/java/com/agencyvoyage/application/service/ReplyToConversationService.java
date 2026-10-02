package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.NotAnAdminException;
import com.agencyvoyage.application.port.in.ReplyToConversationCommand;
import com.agencyvoyage.application.port.in.ReplyToConversationUseCase;
import com.agencyvoyage.application.port.out.ContactMessageRepository;
import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.support.ContactMessageId;
import java.time.Clock;
import java.util.Objects;

public final class ReplyToConversationService implements ReplyToConversationUseCase {

    private final ContactMessageRepository contactMessageRepository;
    private final Clock clock;

    public ReplyToConversationService(ContactMessageRepository contactMessageRepository, Clock clock) {
        this.contactMessageRepository =
                Objects.requireNonNull(contactMessageRepository, "contactMessageRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public ContactMessage reply(ReplyToConversationCommand command) {
        if (!command.admin().isAdmin()) {
            throw new NotAnAdminException();
        }

        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                command.conversationUserId(),
                command.admin().id(),
                command.admin().displayName(),
                command.admin().email(),
                true,
                command.message(),
                clock.instant());

        contactMessageRepository.save(message);

        return message;
    }
}
