package com.agencyvoyage.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participant")
public class ParticipantJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "referred_by_participant_id")
    private UUID referredByParticipantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_booking_id", nullable = false)
    private GroupBookingJpaEntity groupBooking;

    protected ParticipantJpaEntity() {
        // JPA
    }

    public ParticipantJpaEntity(UUID id, UUID userId, String customerName, Instant joinedAt) {
        this(id, userId, customerName, joinedAt, null);
    }

    public ParticipantJpaEntity(
            UUID id, UUID userId, String customerName, Instant joinedAt, UUID referredByParticipantId) {
        this.id = id;
        this.userId = userId;
        this.customerName = customerName;
        this.joinedAt = joinedAt;
        this.referredByParticipantId = referredByParticipantId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public UUID getReferredByParticipantId() {
        return referredByParticipantId;
    }

    void setGroupBooking(GroupBookingJpaEntity groupBooking) {
        this.groupBooking = groupBooking;
    }
}
