package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.support.ContactMessage;

/** Only an admin may reply into a customer's conversation. */
public interface ReplyToConversationUseCase {

    ContactMessage reply(ReplyToConversationCommand command);
}
