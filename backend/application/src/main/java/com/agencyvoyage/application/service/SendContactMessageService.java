package com.agencyvoyage.application.service;

import com.agencyvoyage.application.port.in.SendContactMessageCommand;
import com.agencyvoyage.application.port.in.SendContactMessageUseCase;
import com.agencyvoyage.application.port.out.ContactMessageRepository;
import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.support.ContactMessageId;
import java.time.Clock;
import java.util.Objects;

public final class SendContactMessageService implements SendContactMessageUseCase {

    private final ContactMessageRepository contactMessageRepository;
    private final Clock clock;

    public SendContactMessageService(ContactMessageRepository contactMessageRepository, Clock clock) {
        this.contactMessageRepository =
                Objects.requireNonNull(contactMessageRepository, "contactMessageRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public ContactMessage sendMessage(SendContactMessageCommand command) {
        ContactMessage message = new ContactMessage(
                ContactMessageId.newId(),
                command.author().id(),
                command.author().displayName(),
                command.author().email(),
                command.subject(),
                command.message(),
                clock.instant());

        contactMessageRepository.save(message);

        return message;
    }
}
