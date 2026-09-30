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

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_booking_id", nullable = false)
    private GroupBookingJpaEntity groupBooking;

    protected ParticipantJpaEntity() {
        // JPA
    }

    public ParticipantJpaEntity(UUID id, String customerName, Instant joinedAt) {
        this.id = id;
        this.customerName = customerName;
        this.joinedAt = joinedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    void setGroupBooking(GroupBookingJpaEntity groupBooking) {
        this.groupBooking = groupBooking;
    }
}
