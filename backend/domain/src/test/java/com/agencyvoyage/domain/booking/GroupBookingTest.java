package com.agencyvoyage.domain.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.agencyvoyage.domain.exception.AlreadyFinalizedException;
import com.agencyvoyage.domain.exception.BookingClosedException;
import com.agencyvoyage.domain.exception.DeadlineExpiredException;
import com.agencyvoyage.domain.exception.FinalizationTooEarlyException;
import com.agencyvoyage.domain.exception.GroupFullException;
import com.agencyvoyage.domain.trip.PriceTier;
import com.agencyvoyage.domain.trip.PricingSchedule;
import com.agencyvoyage.domain.trip.Trip;
import com.agencyvoyage.domain.trip.TripId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;

class GroupBookingTest {

    private static final Instant NOW = Instant.parse("2027-01-01T00:00:00Z");

    @Test
    void openingAddsTheCreatorAsTheFirstParticipant() {
        Trip trip = trip(NOW.plus(1, ChronoUnit.DAYS), 2, 5);

        GroupBooking booking = GroupBooking.open(GroupBookingId.newId(), trip, participant("Alice", NOW), NOW);

        assertThat(booking.status()).isEqualTo(GroupBookingStatus.OPEN);
        assertThat(booking.currentParticipantCount()).isEqualTo(1);
        assertThat(booking.participants()).extracting(Participant::customerName).containsExactly("Alice");
    }

    @Test
    void cannotOpenAfterTheTripDeadlineHasPassed() {
        Trip trip = trip(NOW.minusSeconds(1), 2, 5);

        assertThatThrownBy(() -> GroupBooking.open(GroupBookingId.newId(), trip, participant("Alice", NOW), NOW))
                .isInstanceOf(DeadlineExpiredException.class);
    }

    @Test
    void joiningAddsAParticipantAndDropsThePrice() {
        Trip trip = trip(NOW.plus(1, ChronoUnit.DAYS), 2, 5);
        GroupBooking booking = GroupBooking.open(GroupBookingId.newId(), trip, participant("Alice", NOW), NOW);
        assertThat(booking.currentPricePerSeat()).isEqualByComparingTo("1000");

        booking.join(participant("Bob", NOW), NOW);

        assertThat(booking.currentParticipantCount()).isEqualTo(2);
        assertThat(booking.currentPricePerSeat()).isEqualByComparingTo("800");
    }

    @Test
    void cannotJoinAFullGroup() {
        Trip trip = trip(NOW.plus(1, ChronoUnit.DAYS), 1, 1);
        GroupBooking booking = GroupBooking.open(GroupBookingId.newId(), trip, participant("Alice", NOW), NOW);

        assertThatThrownBy(() -> booking.join(participant("Bob", NOW), NOW))
                .isInstanceOf(GroupFullException.class);
    }

    @Test
    void cannotJoinAfterTheDeadline() {
        Trip trip = trip(NOW.plus(1, ChronoUnit.DAYS), 2, 5);
        GroupBooking booking = GroupBooking.open(GroupBookingId.newId(), trip, participant("Alice", NOW), NOW);

        Instant afterDeadline = NOW.plus(2, ChronoUnit.DAYS);
        assertThatThrownBy(() -> booking.join(participant("Bob", NOW), afterDeadline))
                .isInstanceOf(DeadlineExpiredException.class);
    }

    @Test
    void cannotJoinAClosedBooking() {
        Trip trip = trip(NOW.plus(1, ChronoUnit.DAYS), 1, 5);
        GroupBooking booking = GroupBooking.open(GroupBookingId.newId(), trip, participant("Alice", NOW), NOW);
        booking.finalizeBooking(NOW.plus(2, ChronoUnit.DAYS));

        assertThatThrownBy(() -> booking.join(participant("Bob", NOW), NOW.plus(2, ChronoUnit.DAYS)))
                .isInstanceOf(BookingClosedException.class);
    }

    @Test
    void finalizeConfirmsWhenMinimumParticipationIsReached() {
        Trip trip = trip(NOW.plus(1, ChronoUnit.DAYS), 2, 5);
        GroupBooking booking = GroupBooking.open(GroupBookingId.newId(), trip, participant("Alice", NOW), NOW);
        booking.join(participant("Bob", NOW), NOW);

        GroupBookingStatus result = booking.finalizeBooking(NOW.plus(2, ChronoUnit.DAYS));

        assertThat(result).isEqualTo(GroupBookingStatus.CONFIRMED);
        assertThat(booking.status()).isEqualTo(GroupBookingStatus.CONFIRMED);
    }

    @Test
    void finalizeCancelsWhenMinimumParticipationIsNotReached() {
        Trip trip = trip(NOW.plus(1, ChronoUnit.DAYS), 3, 5);
        GroupBooking booking = GroupBooking.open(GroupBookingId.newId(), trip, participant("Alice", NOW), NOW);

        GroupBookingStatus result = booking.finalizeBooking(NOW.plus(2, ChronoUnit.DAYS));

        assertThat(result).isEqualTo(GroupBookingStatus.CANCELLED);
    }

    @Test
    void cannotFinalizeBeforeTheDeadline() {
        Trip trip = trip(NOW.plus(1, ChronoUnit.DAYS), 2, 5);
        GroupBooking booking = GroupBooking.open(GroupBookingId.newId(), trip, participant("Alice", NOW), NOW);

        assertThatThrownBy(() -> booking.finalizeBooking(NOW))
                .isInstanceOf(FinalizationTooEarlyException.class);
    }

    @Test
    void cannotFinalizeTwice() {
        Trip trip = trip(NOW.plus(1, ChronoUnit.DAYS), 1, 5);
        GroupBooking booking = GroupBooking.open(GroupBookingId.newId(), trip, participant("Alice", NOW), NOW);
        booking.finalizeBooking(NOW.plus(2, ChronoUnit.DAYS));

        assertThatThrownBy(() -> booking.finalizeBooking(NOW.plus(3, ChronoUnit.DAYS)))
                .isInstanceOf(AlreadyFinalizedException.class);
    }

    private static Trip trip(Instant deadline, int minParticipants, int maxParticipants) {
        List<PriceTier> tiers = maxParticipants >= 2
                ? List.of(new PriceTier(2, new BigDecimal("800")))
                : List.of();
        PricingSchedule schedule = PricingSchedule.of(new BigDecimal("1000"), tiers, maxParticipants);
        return new Trip(
                TripId.newId(),
                "Bali",
                "10 days in Bali",
                LocalDate.of(2027, 6, 10),
                LocalDate.of(2027, 6, 20),
                minParticipants,
                maxParticipants,
                deadline,
                schedule);
    }

    private static Participant participant(String name, Instant joinedAt) {
        return new Participant(ParticipantId.newId(), name, joinedAt);
    }
}
