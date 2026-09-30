package com.agencyvoyage.infrastructure.persistence.jpa.mapper;

import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;
import com.agencyvoyage.domain.booking.Participant;
import com.agencyvoyage.domain.booking.ParticipantId;
import com.agencyvoyage.domain.trip.PriceTier;
import com.agencyvoyage.domain.trip.PricingSchedule;
import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.infrastructure.persistence.jpa.entity.GroupBookingJpaEntity;
import com.agencyvoyage.infrastructure.persistence.jpa.entity.ParticipantJpaEntity;
import com.agencyvoyage.infrastructure.persistence.jpa.entity.PriceTierEmbeddable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class GroupBookingMapper {

    private GroupBookingMapper() {
    }

    public static GroupBookingJpaEntity toEntity(GroupBooking booking) {
        List<PriceTierEmbeddable> tiers = booking.pricingSchedule().tiers().stream()
                .map(tier -> new PriceTierEmbeddable(tier.minParticipants(), tier.pricePerSeat()))
                .toList();
        List<ParticipantJpaEntity> participants = booking.participants().stream()
                .map(p -> new ParticipantJpaEntity(p.id().value(), p.customerName(), p.joinedAt()))
                .toList();
        return new GroupBookingJpaEntity(
                booking.id().value(),
                booking.tripId().value(),
                booking.status(),
                booking.minParticipants(),
                booking.maxParticipants(),
                booking.deadline(),
                booking.pricingSchedule().basePrice(),
                tiers,
                participants);
    }

    /**
     * Applies the in-memory aggregate's current state onto an already-managed JPA
     * entity: removes rows for participants who left (triggers orphanRemoval) and
     * appends rows for participants not yet persisted. Participants are immutable
     * once created, so an already-managed one is never touched - re-adding it as a
     * fresh Java object would make Hibernate see two conflicting instances for the
     * same id.
     */
    public static void updateEntity(GroupBookingJpaEntity entity, GroupBooking booking) {
        entity.setStatus(booking.status());

        Set<UUID> stillPresent = booking.participants().stream()
                .map(p -> p.id().value())
                .collect(Collectors.toSet());
        entity.getParticipants().removeIf(existing -> !stillPresent.contains(existing.getId()));

        Set<UUID> alreadyPersisted = new HashSet<>();
        for (ParticipantJpaEntity existing : entity.getParticipants()) {
            alreadyPersisted.add(existing.getId());
        }

        for (Participant participant : booking.participants()) {
            if (!alreadyPersisted.contains(participant.id().value())) {
                entity.addParticipant(new ParticipantJpaEntity(
                        participant.id().value(), participant.customerName(), participant.joinedAt()));
            }
        }
    }

    public static GroupBooking toDomain(GroupBookingJpaEntity entity) {
        List<PriceTier> tiers = entity.getPriceTiers().stream()
                .map(tier -> new PriceTier(tier.getMinParticipants(), tier.getPricePerSeat()))
                .toList();
        PricingSchedule schedule = PricingSchedule.of(entity.getBasePrice(), tiers, entity.getMaxParticipants());
        List<Participant> participants = entity.getParticipants().stream()
                .map(p -> new Participant(new ParticipantId(p.getId()), p.getCustomerName(), p.getJoinedAt()))
                .toList();
        return GroupBooking.reconstitute(
                new GroupBookingId(entity.getId()),
                new TripId(entity.getTripId()),
                entity.getMinParticipants(),
                entity.getMaxParticipants(),
                entity.getDeadline(),
                schedule,
                entity.getStatus(),
                participants);
    }
}
