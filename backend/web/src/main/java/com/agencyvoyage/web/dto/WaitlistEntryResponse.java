package com.agencyvoyage.web.dto;

import com.agencyvoyage.domain.booking.WaitlistEntry;
import java.time.Instant;

public record WaitlistEntryResponse(String id, String customerName, Instant joinedAt) {

    public static WaitlistEntryResponse from(WaitlistEntry entry) {
        return new WaitlistEntryResponse(entry.id().toString(), entry.customerName(), entry.joinedAt());
    }
}
