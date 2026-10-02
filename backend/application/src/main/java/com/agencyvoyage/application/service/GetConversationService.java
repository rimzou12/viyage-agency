package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.ConversationAccessDeniedException;
import com.agencyvoyage.application.port.in.GetConversationCommand;
import com.agencyvoyage.application.port.in.GetConversationUseCase;
import com.agencyvoyage.application.port.out.ContactMessageRepository;
import com.agencyvoyage.domain.support.ContactMessage;
import java.util.List;
import java.util.Objects;

public final class GetConversationService implements GetConversationUseCase {

    private final ContactMessageRepository contactMessageRepository;

    public GetConversationService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository =
                Objects.requireNonNull(contactMessageRepository, "contactMessageRepository must not be null");
    }

    @Override
    public List<ContactMessage> getConversation(GetConversationCommand command) {
        boolean isOwnConversation = command.requestedBy().id().equals(command.conversationUserId());
        if (!isOwnConversation && !command.requestedBy().isAdmin()) {
            throw new ConversationAccessDeniedException();
        }

        return contactMessageRepository.findByConversationUserId(command.conversationUserId());
    }
}
