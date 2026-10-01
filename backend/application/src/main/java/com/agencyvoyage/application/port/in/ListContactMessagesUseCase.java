package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.support.ContactMessage;
import java.util.List;

public interface ListContactMessagesUseCase {

    /** Newest first. */
    List<ContactMessage> listMessages();
}
