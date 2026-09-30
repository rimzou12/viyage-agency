package com.agencyvoyage.infrastructure.persistence.jpa.repository;

import com.agencyvoyage.infrastructure.persistence.jpa.entity.AuditEventJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAuditEventJpaRepository extends JpaRepository<AuditEventJpaEntity, UUID> {

    List<AuditEventJpaEntity> findByBookingIdOrderByOccurredAtAsc(UUID bookingId);
}
