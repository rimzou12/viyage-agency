package com.agencyvoyage.infrastructure.persistence.jpa.adapter;

import com.agencyvoyage.application.port.out.TripRepository;
import com.agencyvoyage.domain.trip.Trip;
import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.infrastructure.persistence.jpa.mapper.TripMapper;
import com.agencyvoyage.infrastructure.persistence.jpa.repository.SpringDataTripJpaRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TripRepositoryAdapter implements TripRepository {

    private final SpringDataTripJpaRepository springDataRepository;

    public TripRepositoryAdapter(SpringDataTripJpaRepository springDataRepository) {
        this.springDataRepository = Objects.requireNonNull(springDataRepository);
    }

    @Override
    public Optional<Trip> findById(TripId id) {
        return springDataRepository.findById(id.value()).map(TripMapper::toDomain);
    }

    @Override
    public List<Trip> findAll() {
        return springDataRepository.findAll().stream().map(TripMapper::toDomain).toList();
    }
}
