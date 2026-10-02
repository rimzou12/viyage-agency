package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.NotAnAdminException;
import com.agencyvoyage.application.port.in.ListContactMessagesUseCase;
import com.agencyvoyage.application.port.out.ContactMessageRepository;
import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.user.User;
import java.util.List;
import java.util.Objects;

public final class ListContactMessagesService implements ListContactMessagesUseCase {

    private final ContactMessageRepository contactMessageRepository;

    public ListContactMessagesService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository =
                Objects.requireNonNull(contactMessageRepository, "contactMessageRepository must not be null");
    }

    @Override
    public List<ContactMessage> listMessages(User requestedBy) {
        if (!requestedBy.isAdmin()) {
            throw new NotAnAdminException();
        }
        return contactMessageRepository.findAll();
    }
}
