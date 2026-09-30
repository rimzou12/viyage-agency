package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.booking.GroupBookingId;
import java.util.List;

public interface AuditTrailRepository {

    void append(AuditEntry entry);

    /** Oldest first. */
    List<AuditEntry> findByBookingId(GroupBookingId bookingId);
}
