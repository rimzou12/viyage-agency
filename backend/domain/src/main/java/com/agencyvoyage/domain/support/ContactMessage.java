package com.agencyvoyage.domain.support;

import com.agencyvoyage.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;

/**
 * One message in a customer's chat thread with the admin. {@code conversationUserId}
 * identifies the thread (always the customer's id, never the admin's) while
 * {@code authorUserId}/{@code fromAdmin} identify who actually wrote this particular
 * message - the customer themselves, or an admin replying into their thread.
 */
public record ContactMessage(
        ContactMessageId id,
        UserId conversationUserId,
        UserId authorUserId,
        String authorName,
        String authorEmail,
        boolean fromAdmin,
        String message,
        Instant sentAt) {

    public ContactMessage {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(conversationUserId, "conversationUserId must not be null");
        Objects.requireNonNull(authorUserId, "authorUserId must not be null");
        Objects.requireNonNull(sentAt, "sentAt must not be null");
        if (authorName == null || authorName.isBlank()) {
            throw new IllegalArgumentException("authorName must not be blank");
        }
        if (authorEmail == null || authorEmail.isBlank()) {
            throw new IllegalArgumentException("authorEmail must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
