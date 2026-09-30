package com.agencyvoyage.domain.booking;

import com.agencyvoyage.domain.exception.AlreadyFinalizedException;
import com.agencyvoyage.domain.exception.AlreadyJoinedException;
import com.agencyvoyage.domain.exception.AlreadyWaitlistedException;
import com.agencyvoyage.domain.exception.BookingClosedException;
import com.agencyvoyage.domain.exception.BookingNotFullException;
import com.agencyvoyage.domain.exception.DeadlineExpiredException;
import com.agencyvoyage.domain.exception.FinalizationTooEarlyException;
import com.agencyvoyage.domain.exception.GroupFullException;
import com.agencyvoyage.domain.exception.NotOnWaitlistException;
import com.agencyvoyage.domain.exception.ParticipantNotInBookingException;
import com.agencyvoyage.domain.trip.PricingSchedule;
import com.agencyvoyage.domain.trip.Trip;
import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.domain.user.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private final List<WaitlistEntry> waitlist = new ArrayList<>();
    private GroupBookingStatus status;

    private GroupBooking(
            GroupBookingId id,
            TripId tripId,
            int minParticipants,
            int maxParticipants,
            Instant deadline,
            PricingSchedule pricingSchedule,
            GroupBookingStatus status,
            List<Participant> participants,
            List<WaitlistEntry> waitlist) {
        this.id = id;
        this.tripId = tripId;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.deadline = deadline;
        this.pricingSchedule = pricingSchedule;
        this.status = status;
        this.participants.addAll(participants);
        this.waitlist.addAll(waitlist);
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
                List.of(),
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
            List<Participant> participants,
            List<WaitlistEntry> waitlist) {
        return new GroupBooking(
                id,
                tripId,
                minParticipants,
                maxParticipants,
                deadline,
                pricingSchedule,
                status,
                participants,
                waitlist);
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
        if (participants.stream().anyMatch(p -> p.userId().equals(participant.userId()))) {
            throw new AlreadyJoinedException(id, participant.userId());
        }
        participants.add(participant);
    }

    /**
     * Removes the given user's participation, freeing their seat and re-pricing the
     * group for everyone left. If anyone is waiting, the longest-waiting entry is
     * automatically promoted into the freed seat and returned so the caller can
     * notify them - this is the only way a seat freed by a departure is filled from
     * the waitlist rather than left open for anyone to grab.
     */
    public Optional<Participant> leave(UserId userId, Instant now) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(now, "now must not be null");

        if (status != GroupBookingStatus.OPEN) {
            throw new BookingClosedException(id, status);
        }
        if (!now.isBefore(deadline)) {
            throw new DeadlineExpiredException(deadline, now);
        }
        boolean removed = participants.removeIf(p -> p.userId().equals(userId));
        if (!removed) {
            throw new ParticipantNotInBookingException(id, userId);
        }

        if (waitlist.isEmpty()) {
            return Optional.empty();
        }
        WaitlistEntry promoted = waitlist.remove(0);
        Participant newParticipant =
                new Participant(ParticipantId.newId(), promoted.userId(), promoted.customerName(), now);
        participants.add(newParticipant);
        return Optional.of(newParticipant);
    }

    /**
     * Joins the waitlist instead of the group itself - only valid once the group is
     * actually full (otherwise {@link #join} should be used directly).
     */
    public void joinWaitlist(WaitlistEntry entry, Instant now) {
        Objects.requireNonNull(entry, "entry must not be null");
        Objects.requireNonNull(now, "now must not be null");

        if (status != GroupBookingStatus.OPEN) {
            throw new BookingClosedException(id, status);
        }
        if (!now.isBefore(deadline)) {
            throw new DeadlineExpiredException(deadline, now);
        }
        if (!isFull()) {
            throw new BookingNotFullException(id);
        }
        if (participants.stream().anyMatch(p -> p.userId().equals(entry.userId()))) {
            throw new AlreadyJoinedException(id, entry.userId());
        }
        if (waitlist.stream().anyMatch(w -> w.userId().equals(entry.userId()))) {
            throw new AlreadyWaitlistedException(id, entry.userId());
        }
        waitlist.add(entry);
    }

    /** Backs out of the waitlist without waiting for a seat. */
    public void leaveWaitlist(UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        boolean removed = waitlist.removeIf(w -> w.userId().equals(userId));
        if (!removed) {
            throw new NotOnWaitlistException(id, userId);
        }
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

    /** Oldest-waiting first. */
    public List<WaitlistEntry> waitlist() {
        return List.copyOf(waitlist);
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
