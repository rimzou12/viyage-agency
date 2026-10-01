package com.agencyvoyage.infrastructure.persistence.jpa.adapter;

import com.agencyvoyage.application.port.out.UserAccount;
import com.agencyvoyage.application.port.out.UserRepository;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import com.agencyvoyage.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.agencyvoyage.infrastructure.persistence.jpa.repository.SpringDataUserJpaRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserJpaRepository springDataRepository;

    public UserRepositoryAdapter(SpringDataUserJpaRepository springDataRepository) {
        this.springDataRepository = Objects.requireNonNull(springDataRepository);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return springDataRepository.findById(id.value()).map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<UserAccount> findAccountByEmail(String email) {
        return springDataRepository.findByEmail(email)
                .map(entity -> new UserAccount(toDomain(entity), entity.getPasswordHash()));
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }

    @Override
    public User createAccount(String email, String hashedPassword, String displayName, boolean isAdmin) {
        UserJpaEntity saved = springDataRepository.save(
                new UserJpaEntity(UUID.randomUUID(), email, hashedPassword, displayName, isAdmin));
        return toDomain(saved);
    }

    private static User toDomain(UserJpaEntity entity) {
        return new User(new UserId(entity.getId()), entity.getEmail(), entity.getDisplayName(), entity.isAdmin());
    }
}
