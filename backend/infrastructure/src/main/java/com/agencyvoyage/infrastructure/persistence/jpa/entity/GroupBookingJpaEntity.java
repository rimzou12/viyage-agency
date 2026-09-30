package com.agencyvoyage.infrastructure.persistence.jpa.entity;

import com.agencyvoyage.domain.booking.GroupBookingStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "group_booking")
public class GroupBookingJpaEntity {

    @Id
    private UUID id;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupBookingStatus status;

    @Column(name = "min_participants", nullable = false)
    private int minParticipants;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(nullable = false)
    private Instant deadline;

    @Column(name = "base_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "group_booking_price_tier", joinColumns = @JoinColumn(name = "group_booking_id"))
    @OrderBy("minParticipants ASC")
    private List<PriceTierEmbeddable> priceTiers = new ArrayList<>();

    @OneToMany(mappedBy = "groupBooking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("joinedAt ASC")
    private List<ParticipantJpaEntity> participants = new ArrayList<>();

    protected GroupBookingJpaEntity() {
        // JPA
    }

    public GroupBookingJpaEntity(
            UUID id,
            UUID tripId,
            GroupBookingStatus status,
            int minParticipants,
            int maxParticipants,
            Instant deadline,
            BigDecimal basePrice,
            List<PriceTierEmbeddable> priceTiers,
            List<ParticipantJpaEntity> participants) {
        this.id = id;
        this.tripId = tripId;
        this.status = status;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.deadline = deadline;
        this.basePrice = basePrice;
        this.priceTiers = new ArrayList<>(priceTiers);
        replaceParticipants(participants);
    }

    /** Sets the initial participant list on a brand-new entity, keeping the bidirectional association consistent. */
    public final void replaceParticipants(List<ParticipantJpaEntity> newParticipants) {
        this.participants.clear();
        for (ParticipantJpaEntity participant : newParticipants) {
            addParticipant(participant);
        }
    }

    /**
     * Appends one participant to an already-managed entity. Existing participant rows
     * are left untouched (they are immutable once created) - replacing the whole
     * collection here would make Hibernate try to re-insert already-managed rows under
     * a colliding id.
     */
    public void addParticipant(ParticipantJpaEntity participant) {
        participant.setGroupBooking(this);
        this.participants.add(participant);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTripId() {
        return tripId;
    }

    public GroupBookingStatus getStatus() {
        return status;
    }

    public void setStatus(GroupBookingStatus status) {
        this.status = status;
    }

    public int getMinParticipants() {
        return minParticipants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public List<PriceTierEmbeddable> getPriceTiers() {
        return priceTiers;
    }

    public List<ParticipantJpaEntity> getParticipants() {
        return participants;
    }
}
