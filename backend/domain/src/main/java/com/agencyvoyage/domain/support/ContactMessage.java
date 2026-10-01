package com.agencyvoyage.domain.support;

import com.agencyvoyage.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;

/** A message a logged-in customer sent to the admin - support inbox, not a group booking concept. */
public record ContactMessage(
        ContactMessageId id,
        UserId authorUserId,
        String authorName,
        String authorEmail,
        String subject,
        String message,
        Instant sentAt) {

    public ContactMessage {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(authorUserId, "authorUserId must not be null");
        Objects.requireNonNull(sentAt, "sentAt must not be null");
        if (authorName == null || authorName.isBlank()) {
            throw new IllegalArgumentException("authorName must not be blank");
        }
        if (authorEmail == null || authorEmail.isBlank()) {
            throw new IllegalArgumentException("authorEmail must not be blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
