package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.support.ContactMessage;
import java.util.List;

public interface ContactMessageRepository {

    void save(ContactMessage message);

    /** Newest first. */
    List<ContactMessage> findAll();
}
