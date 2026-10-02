package com.agencyvoyage.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contact_message")
public class ContactMessageJpaEntity {

    @Id
    private UUID id;

    @Column(name = "conversation_user_id", nullable = false)
    private UUID conversationUserId;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "author_email", nullable = false)
    private String authorEmail;

    @Column(name = "from_admin", nullable = false)
    private boolean fromAdmin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    protected ContactMessageJpaEntity() {
        // JPA
    }

    public ContactMessageJpaEntity(
            UUID id,
            UUID conversationUserId,
            UUID authorUserId,
            String authorName,
            String authorEmail,
            boolean fromAdmin,
            String message,
            Instant sentAt) {
        this.id = id;
        this.conversationUserId = conversationUserId;
        this.authorUserId = authorUserId;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.fromAdmin = fromAdmin;
        this.message = message;
        this.sentAt = sentAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getConversationUserId() {
        return conversationUserId;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public boolean isFromAdmin() {
        return fromAdmin;
    }

    public String getMessage() {
        return message;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
