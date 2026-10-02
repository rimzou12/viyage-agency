package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.user.User;
import java.util.List;

public interface ListContactMessagesUseCase {

    /** Newest first, across every conversation. Admin only. */
    List<ContactMessage> listMessages(User requestedBy);
}
