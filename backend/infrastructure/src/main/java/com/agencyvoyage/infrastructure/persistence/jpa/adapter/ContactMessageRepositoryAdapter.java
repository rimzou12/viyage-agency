package com.agencyvoyage.infrastructure.persistence.jpa.adapter;

import com.agencyvoyage.application.port.out.ContactMessageRepository;
import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.support.ContactMessageId;
import com.agencyvoyage.domain.user.UserId;
import com.agencyvoyage.infrastructure.persistence.jpa.entity.ContactMessageJpaEntity;
import com.agencyvoyage.infrastructure.persistence.jpa.repository.SpringDataContactMessageJpaRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class ContactMessageRepositoryAdapter implements ContactMessageRepository {

    private final SpringDataContactMessageJpaRepository springDataRepository;

    public ContactMessageRepositoryAdapter(SpringDataContactMessageJpaRepository springDataRepository) {
        this.springDataRepository =
                Objects.requireNonNull(springDataRepository, "springDataRepository must not be null");
    }

    @Override
    public void save(ContactMessage message) {
        springDataRepository.save(new ContactMessageJpaEntity(
                message.id().value(),
                message.authorUserId().value(),
                message.authorName(),
                message.authorEmail(),
                message.subject(),
                message.message(),
                message.sentAt()));
    }

    @Override
    public List<ContactMessage> findAll() {
        return springDataRepository.findAllByOrderBySentAtDesc().stream()
                .map(ContactMessageRepositoryAdapter::toDomain)
                .toList();
    }

    private static ContactMessage toDomain(ContactMessageJpaEntity entity) {
        return new ContactMessage(
                new ContactMessageId(entity.getId()),
                new UserId(entity.getAuthorUserId()),
                entity.getAuthorName(),
                entity.getAuthorEmail(),
                entity.getSubject(),
                entity.getMessage(),
                entity.getSentAt());
    }
}
