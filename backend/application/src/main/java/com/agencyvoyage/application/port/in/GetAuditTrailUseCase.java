package com.agencyvoyage.application.port.in;

import com.agencyvoyage.application.port.out.AuditEntry;
import com.agencyvoyage.domain.booking.GroupBookingId;
import java.util.List;

public interface GetAuditTrailUseCase {

    /** Oldest first. Empty if the booking has no recorded history (or doesn't exist). */
    List<AuditEntry> getAuditTrail(GroupBookingId bookingId);
}
