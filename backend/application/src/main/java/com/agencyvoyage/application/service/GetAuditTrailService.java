package com.agencyvoyage.application.service;

import com.agencyvoyage.application.port.in.GetAuditTrailUseCase;
import com.agencyvoyage.application.port.out.AuditEntry;
import com.agencyvoyage.application.port.out.AuditTrailRepository;
import com.agencyvoyage.domain.booking.GroupBookingId;
import java.util.List;
import java.util.Objects;

public final class GetAuditTrailService implements GetAuditTrailUseCase {

    private final AuditTrailRepository auditTrailRepository;

    public GetAuditTrailService(AuditTrailRepository auditTrailRepository) {
        this.auditTrailRepository = Objects.requireNonNull(auditTrailRepository, "auditTrailRepository must not be null");
    }

    @Override
    public List<AuditEntry> getAuditTrail(GroupBookingId bookingId) {
        return auditTrailRepository.findByBookingId(bookingId);
    }
}
