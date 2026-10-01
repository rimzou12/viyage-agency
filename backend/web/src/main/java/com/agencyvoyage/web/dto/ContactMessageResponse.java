package com.agencyvoyage.web.dto;

import com.agencyvoyage.domain.support.ContactMessage;
import java.time.Instant;

public record ContactMessageResponse(
        String id, String authorName, String authorEmail, String subject, String message, Instant sentAt) {

    public static ContactMessageResponse from(ContactMessage contactMessage) {
        return new ContactMessageResponse(
                contactMessage.id().toString(),
                contactMessage.authorName(),
                contactMessage.authorEmail(),
                contactMessage.subject(),
                contactMessage.message(),
                contactMessage.sentAt());
    }
}
