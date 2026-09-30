package com.agencyvoyage.domain.booking;

import com.agencyvoyage.domain.exception.AlreadyFinalizedException;
import com.agencyvoyage.domain.exception.BookingClosedException;
import com.agencyvoyage.domain.exception.DeadlineExpiredException;
import com.agencyvoyage.domain.exception.FinalizationTooEarlyException;
import com.agencyvoyage.domain.exception.GroupFullException;
import com.agencyvoyage.domain.trip.PricingSchedule;
import com.agencyvoyage.domain.trip.Trip;
import com.agencyvoyage.domain.trip.TripId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * One group of customers pooling together on a {@link Trip}. Captures the trip's
 * min/max participants, deadline and pricing schedule at the moment it is opened, so
 * that later changes to the trip catalog do not retroactively affect an in-flight group.
 */
public final class GroupBooking {

    private final GroupBookingId id;
    private final TripId tripId;
    private final int minParticipants;
    private final int maxParticipants;
    private final Instant deadline;
    private final PricingSchedule pricingSchedule;
    private final List<Participant> participants = new ArrayList<>();
    private GroupBookingStatus status;

    private GroupBooking(
            GroupBookingId id,
            TripId tripId,
            int minParticipants,
            int maxParticipants,
            Instant deadline,
            PricingSchedule pricingSchedule,
            GroupBookingStatus status,
            List<Participant> participants) {
        this.id = id;
        this.tripId = tripId;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.deadline = deadline;
        this.pricingSchedule = pricingSchedule;
        this.status = status;
        this.participants.addAll(participants);
    }

    /** Opens a new group booking for {@code trip}, with {@code creator} as its first participant. */
    public static GroupBooking open(GroupBookingId id, Trip trip, Participant creator, Instant now) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(trip, "trip must not be null");
        Objects.requireNonNull(creator, "creator must not be null");
        Objects.requireNonNull(now, "now must not be null");

        if (!trip.acceptsNewGroupBookingsAt(now)) {
            throw new DeadlineExpiredException(trip.bookingDeadline(), now);
        }

        GroupBooking booking = new GroupBooking(
                id,
                trip.id(),
                trip.minParticipants(),
                trip.maxParticipants(),
                trip.bookingDeadline(),
                trip.pricingSchedule(),
                GroupBookingStatus.OPEN,
                List.of());
        booking.participants.add(creator);
        return booking;
    }

    /** Reconstructs a booking from persisted state - does not re-run creation-time validation. */
    public static GroupBooking reconstitute(
            GroupBookingId id,
            TripId tripId,
            int minParticipants,
            int maxParticipants,
            Instant deadline,
            PricingSchedule pricingSchedule,
            GroupBookingStatus status,
            List<Participant> participants) {
        return new GroupBooking(
                id, tripId, minParticipants, maxParticipants, deadline, pricingSchedule, status, participants);
    }

    public void join(Participant participant, Instant now) {
        Objects.requireNonNull(participant, "participant must not be null");
        Objects.requireNonNull(now, "now must not be null");

        if (status != GroupBookingStatus.OPEN) {
            throw new BookingClosedException(id, status);
        }
        if (!now.isBefore(deadline)) {
            throw new DeadlineExpiredException(deadline, now);
        }
        if (participants.size() >= maxParticipants) {
            throw new GroupFullException(id, maxParticipants);
        }
        participants.add(participant);
    }

    /** Confirms or cancels the group once its deadline has passed, based on minimum participation. */
    public GroupBookingStatus finalizeBooking(Instant now) {
        Objects.requireNonNull(now, "now must not be null");

        if (status != GroupBookingStatus.OPEN) {
            throw new AlreadyFinalizedException(id, status);
        }
        if (now.isBefore(deadline)) {
            throw new FinalizationTooEarlyException(id, deadline, now);
        }
        status = participants.size() >= minParticipants
                ? GroupBookingStatus.CONFIRMED
                : GroupBookingStatus.CANCELLED;
        return status;
    }

    public int currentParticipantCount() {
        return participants.size();
    }

    public BigDecimal currentPricePerSeat() {
        return pricingSchedule.priceFor(currentParticipantCount());
    }

    public boolean isFull() {
        return participants.size() >= maxParticipants;
    }

    public GroupBookingId id() {
        return id;
    }

    public TripId tripId() {
        return tripId;
    }

    public GroupBookingStatus status() {
        return status;
    }

    public int minParticipants() {
        return minParticipants;
    }

    public int maxParticipants() {
        return maxParticipants;
    }

    public Instant deadline() {
        return deadline;
    }

    public PricingSchedule pricingSchedule() {
        return pricingSchedule;
    }

    public List<Participant> participants() {
        return List.copyOf(participants);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupBooking that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
