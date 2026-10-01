package com.agencyvoyage.infrastructure.persistence.jpa.repository;

import com.agencyvoyage.infrastructure.persistence.jpa.entity.ContactMessageJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataContactMessageJpaRepository extends JpaRepository<ContactMessageJpaEntity, UUID> {

    List<ContactMessageJpaEntity> findAllByOrderBySentAtDesc();
}
