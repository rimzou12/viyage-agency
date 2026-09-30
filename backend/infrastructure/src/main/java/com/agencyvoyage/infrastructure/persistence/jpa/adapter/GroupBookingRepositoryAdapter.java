package com.agencyvoyage.infrastructure.persistence.jpa.adapter;

import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;
import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.infrastructure.persistence.jpa.entity.GroupBookingJpaEntity;
import com.agencyvoyage.infrastructure.persistence.jpa.mapper.GroupBookingMapper;
import com.agencyvoyage.infrastructure.persistence.jpa.repository.SpringDataGroupBookingJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GroupBookingRepositoryAdapter implements GroupBookingRepository {

    private final SpringDataGroupBookingJpaRepository springDataRepository;

    public GroupBookingRepositoryAdapter(SpringDataGroupBookingJpaRepository springDataRepository) {
        this.springDataRepository = Objects.requireNonNull(springDataRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupBooking> findById(GroupBookingId id) {
        return springDataRepository.findById(id.value()).map(GroupBookingMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupBooking> findByTripId(TripId tripId) {
        return springDataRepository.findByTripId(tripId.value()).stream()
                .map(GroupBookingMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupBooking> findOpenWithDeadlineAtOrBefore(Instant instant) {
        return springDataRepository
                .findByStatusAndDeadlineLessThanEqual(GroupBookingStatus.OPEN, instant)
                .stream()
                .map(GroupBookingMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void save(GroupBooking booking) {
        Optional<GroupBookingJpaEntity> existing = springDataRepository.findById(booking.id().value());
        if (existing.isPresent()) {
            GroupBookingMapper.updateEntity(existing.get(), booking);
        } else {
            springDataRepository.save(GroupBookingMapper.toEntity(booking));
        }
    }
}
