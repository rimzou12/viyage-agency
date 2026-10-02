package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import java.util.Objects;

public record GetConversationCommand(UserId conversationUserId, User requestedBy) {

    public GetConversationCommand {
        Objects.requireNonNull(conversationUserId, "conversationUserId must not be null");
        Objects.requireNonNull(requestedBy, "requestedBy must not be null");
    }
}
