package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.support.ContactMessage;
import java.util.List;

public interface GetConversationUseCase {

    /** Oldest first (chat order). Only the conversation's own customer or an admin may view it. */
    List<ContactMessage> getConversation(GetConversationCommand command);
}
