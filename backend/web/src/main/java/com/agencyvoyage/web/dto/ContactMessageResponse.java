package com.agencyvoyage.web.dto;

import com.agencyvoyage.domain.support.ContactMessage;
import java.time.Instant;

public record ContactMessageResponse(
        String id,
        String conversationUserId,
        String authorName,
        String authorEmail,
        boolean fromAdmin,
        String message,
        Instant sentAt) {

    public static ContactMessageResponse from(ContactMessage contactMessage) {
        return new ContactMessageResponse(
                contactMessage.id().toString(),
                contactMessage.conversationUserId().toString(),
                contactMessage.authorName(),
                contactMessage.authorEmail(),
                contactMessage.fromAdmin(),
                contactMessage.message(),
                contactMessage.sentAt());
    }
}
