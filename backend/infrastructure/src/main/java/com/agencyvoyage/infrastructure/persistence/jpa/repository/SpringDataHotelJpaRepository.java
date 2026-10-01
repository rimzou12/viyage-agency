package com.agencyvoyage.infrastructure.persistence.jpa.repository;

import com.agencyvoyage.infrastructure.persistence.jpa.entity.HotelJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataHotelJpaRepository extends JpaRepository<HotelJpaEntity, UUID> {

    List<HotelJpaEntity> findByTripId(UUID tripId);
}
