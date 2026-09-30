package com.agencyvoyage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.agencyvoyage.application.port.out.AuditEntry;
import com.agencyvoyage.application.port.out.AuditEventType;
import com.agencyvoyage.application.port.out.AuditTrailRepository;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.ParticipantId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAuditTrailServiceTest {

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @Test
    void returnsTheStoredEntriesForABooking() {
        GetAuditTrailService service = new GetAuditTrailService(auditTrailRepository);
        GroupBookingId bookingId = GroupBookingId.newId();
        AuditEntry entry = new AuditEntry(
                bookingId,
                AuditEventType.PARTICIPANT_JOINED,
                ParticipantId.newId(),
                "Alice",
                1,
                new BigDecimal("1000"),
                null,
                Instant.parse("2027-01-01T00:00:00Z"));
        when(auditTrailRepository.findByBookingId(bookingId)).thenReturn(List.of(entry));

        assertThat(service.getAuditTrail(bookingId)).containsExactly(entry);
    }

    @Test
    void returnsAnEmptyListWhenThereIsNoHistory() {
        GetAuditTrailService service = new GetAuditTrailService(auditTrailRepository);
        GroupBookingId bookingId = GroupBookingId.newId();
        when(auditTrailRepository.findByBookingId(bookingId)).thenReturn(List.of());

        assertThat(service.getAuditTrail(bookingId)).isEmpty();
    }
}
