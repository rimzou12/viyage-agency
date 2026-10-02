package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import java.util.Objects;

public record ReplyToConversationCommand(UserId conversationUserId, User admin, String message) {

    public ReplyToConversationCommand {
        Objects.requireNonNull(conversationUserId, "conversationUserId must not be null");
        Objects.requireNonNull(admin, "admin must not be null");
    }
}
