package com.agencyvoyage.infrastructure.persistence.jpa.repository;

import com.agencyvoyage.infrastructure.persistence.jpa.entity.TripJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTripJpaRepository extends JpaRepository<TripJpaEntity, UUID> {
}
