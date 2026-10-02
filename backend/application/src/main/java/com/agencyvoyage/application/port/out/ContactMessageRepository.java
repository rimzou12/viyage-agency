package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.user.UserId;
import java.util.List;

public interface ContactMessageRepository {

    void save(ContactMessage message);

    /** Newest first, across every conversation. */
    List<ContactMessage> findAll();

    /** Oldest first (chat order), for one customer's thread. */
    List<ContactMessage> findByConversationUserId(UserId conversationUserId);
}
