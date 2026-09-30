package com.agencyvoyage.web.dto;

import com.agencyvoyage.application.port.out.AuditEntry;
import java.math.BigDecimal;
import java.time.Instant;

public record AuditEventResponse(
        String type,
        String participantId,
        String customerName,
        int participantCount,
        BigDecimal pricePerSeat,
        String status,
        Instant occurredAt) {

    public static AuditEventResponse from(AuditEntry entry) {
        return new AuditEventResponse(
                entry.type().name(),
                entry.participantId() == null ? null : entry.participantId().toString(),
                entry.customerName(),
                entry.participantCount(),
                entry.pricePerSeat(),
                entry.status() == null ? null : entry.status().name(),
                entry.occurredAt());
    }
}
