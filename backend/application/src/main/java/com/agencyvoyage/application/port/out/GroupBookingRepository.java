package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.trip.TripId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GroupBookingRepository {

    Optional<GroupBooking> findById(GroupBookingId id);

    List<GroupBooking> findByTripId(TripId tripId);

    /** Bookings still OPEN whose deadline is at or before {@code instant} - candidates for finalization. */
    List<GroupBooking> findOpenWithDeadlineAtOrBefore(Instant instant);

    void save(GroupBooking booking);
}
