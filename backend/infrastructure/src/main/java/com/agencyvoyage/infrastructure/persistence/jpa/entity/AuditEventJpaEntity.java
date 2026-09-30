package com.agencyvoyage.infrastructure.persistence.jpa.entity;

import com.agencyvoyage.application.port.out.AuditEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_event")
public class AuditEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AuditEventType eventType;

    @Column(name = "participant_id")
    private UUID participantId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "participant_count", nullable = false)
    private int participantCount;

    @Column(name = "price_per_seat")
    private BigDecimal pricePerSeat;

    private String status;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditEventJpaEntity() {
        // JPA
    }

    public AuditEventJpaEntity(
            UUID id,
            UUID bookingId,
            AuditEventType eventType,
            UUID participantId,
            String customerName,
            int participantCount,
            BigDecimal pricePerSeat,
            String status,
            Instant occurredAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.eventType = eventType;
        this.participantId = participantId;
        this.customerName = customerName;
        this.participantCount = participantCount;
        this.pricePerSeat = pricePerSeat;
        this.status = status;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public BigDecimal getPricePerSeat() {
        return pricePerSeat;
    }

    public String getStatus() {
        return status;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
