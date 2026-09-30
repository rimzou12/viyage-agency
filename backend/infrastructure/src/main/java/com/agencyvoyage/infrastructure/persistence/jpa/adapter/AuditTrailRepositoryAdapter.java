package com.agencyvoyage.infrastructure.persistence.jpa.adapter;

import com.agencyvoyage.application.port.out.AuditEntry;
import com.agencyvoyage.application.port.out.AuditTrailRepository;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;
import com.agencyvoyage.domain.booking.ParticipantId;
import com.agencyvoyage.infrastructure.persistence.jpa.entity.AuditEventJpaEntity;
import com.agencyvoyage.infrastructure.persistence.jpa.repository.SpringDataAuditEventJpaRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailRepositoryAdapter implements AuditTrailRepository {

    private final SpringDataAuditEventJpaRepository springDataRepository;

    public AuditTrailRepositoryAdapter(SpringDataAuditEventJpaRepository springDataRepository) {
        this.springDataRepository = Objects.requireNonNull(springDataRepository, "springDataRepository must not be null");
    }

    @Override
    public void append(AuditEntry entry) {
        springDataRepository.save(new AuditEventJpaEntity(
                UUID.randomUUID(),
                entry.bookingId().value(),
                entry.type(),
                entry.participantId() == null ? null : entry.participantId().value(),
                entry.customerName(),
                entry.participantCount(),
                entry.pricePerSeat(),
                entry.status() == null ? null : entry.status().name(),
                entry.occurredAt()));
    }

    @Override
    public List<AuditEntry> findByBookingId(GroupBookingId bookingId) {
        return springDataRepository.findByBookingIdOrderByOccurredAtAsc(bookingId.value()).stream()
                .map(AuditTrailRepositoryAdapter::toDomain)
                .toList();
    }

    private static AuditEntry toDomain(AuditEventJpaEntity entity) {
        return new AuditEntry(
                new GroupBookingId(entity.getBookingId()),
                entity.getEventType(),
                entity.getParticipantId() == null ? null : new ParticipantId(entity.getParticipantId()),
                entity.getCustomerName(),
                entity.getParticipantCount(),
                entity.getPricePerSeat(),
                entity.getStatus() == null ? null : GroupBookingStatus.valueOf(entity.getStatus()),
                entity.getOccurredAt());
    }
}
