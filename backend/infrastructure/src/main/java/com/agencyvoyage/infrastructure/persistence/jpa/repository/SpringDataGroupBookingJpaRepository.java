package com.agencyvoyage.infrastructure.persistence.jpa.repository;

import com.agencyvoyage.domain.booking.GroupBookingStatus;
import com.agencyvoyage.infrastructure.persistence.jpa.entity.GroupBookingJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataGroupBookingJpaRepository extends JpaRepository<GroupBookingJpaEntity, UUID> {

    List<GroupBookingJpaEntity> findByTripId(UUID tripId);

    List<GroupBookingJpaEntity> findByStatusAndDeadlineLessThanEqual(GroupBookingStatus status, Instant deadline);
}
