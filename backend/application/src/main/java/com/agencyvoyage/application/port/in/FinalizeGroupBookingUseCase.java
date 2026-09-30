package com.agencyvoyage.application.port.in;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.GroupBookingStatus;
import com.agencyvoyage.domain.exception.AlreadyFinalizedException;
import com.agencyvoyage.domain.exception.FinalizationTooEarlyException;
import java.time.Instant;
import java.util.List;

public interface FinalizeGroupBookingUseCase {

    /**
     * @throws GroupBookingNotFoundException if the booking does not exist
     * @throws AlreadyFinalizedException if the booking is no longer OPEN
     * @throws FinalizationTooEarlyException if the booking's deadline has not passed yet
     */
    GroupBookingStatus finalizeGroupBooking(GroupBookingId id);

    /** Finalizes every OPEN booking whose deadline has passed. Returns the ids that were finalized. */
    List<GroupBookingId> finalizeAllDue(Instant now);
}
