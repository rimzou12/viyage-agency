package com.agencyvoyage.application.exception;

public final class ConversationAccessDeniedException extends RuntimeException {

    public ConversationAccessDeniedException() {
        super("You may only view your own conversation with the admin");
    }
}
