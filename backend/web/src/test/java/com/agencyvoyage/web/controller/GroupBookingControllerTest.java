package com.agencyvoyage.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.application.port.in.CreateGroupBookingUseCase;
import com.agencyvoyage.application.port.in.GetGroupBookingUseCase;
import com.agencyvoyage.application.port.in.JoinGroupBookingCommand;
import com.agencyvoyage.application.port.in.JoinGroupBookingUseCase;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.Participant;
import com.agencyvoyage.domain.booking.ParticipantId;
import com.agencyvoyage.domain.exception.GroupFullException;
import com.agencyvoyage.domain.trip.PricingSchedule;
import com.agencyvoyage.domain.trip.Trip;
import com.agencyvoyage.domain.trip.TripId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(GroupBookingController.class)
class GroupBookingControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CreateGroupBookingUseCase createGroupBookingUseCase;

    @MockitoBean
    private JoinGroupBookingUseCase joinGroupBookingUseCase;

    @MockitoBean
    private GetGroupBookingUseCase getGroupBookingUseCase;

    @Test
    void rejectsAJoinRequestWithABlankCustomerName() {
        GroupBookingId bookingId = GroupBookingId.newId();

        assertThat(mvc.post()
                        .uri("/api/group-bookings/" + bookingId + "/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"  \"}"))
                .hasStatus(400);
    }

    @Test
    void returns409WhenTheDomainRejectsTheJoin() {
        GroupBookingId bookingId = GroupBookingId.newId();
        when(joinGroupBookingUseCase.joinGroupBooking(any(JoinGroupBookingCommand.class)))
                .thenThrow(new GroupFullException(bookingId, 5));

        assertThat(mvc.post()
                        .uri("/api/group-bookings/" + bookingId + "/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Bob\"}"))
                .hasStatus(409);
    }

    @Test
    void returns404WhenTheBookingDoesNotExist() {
        GroupBookingId unknownId = GroupBookingId.newId();
        when(getGroupBookingUseCase.getGroupBooking(unknownId))
                .thenThrow(new GroupBookingNotFoundException(unknownId));

        assertThat(mvc.get().uri("/api/group-bookings/" + unknownId)).hasStatus(404);
    }

    @Test
    void getReturnsTheBookingAsJson() {
        GroupBookingId bookingId = GroupBookingId.newId();
        GroupBooking booking = booking(bookingId);
        when(getGroupBookingUseCase.getGroupBooking(bookingId)).thenReturn(booking);

        assertThat(mvc.get().uri("/api/group-bookings/" + bookingId))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.status")
                .isEqualTo("OPEN");
    }

    private static GroupBooking booking(GroupBookingId id) {
        PricingSchedule schedule = PricingSchedule.of(new BigDecimal("1000"), List.of(), 5);
        Trip trip = new Trip(
                TripId.newId(),
                "Bali",
                "desc",
                LocalDate.of(2027, 6, 10),
                LocalDate.of(2027, 6, 20),
                2,
                5,
                Instant.now().plus(30, ChronoUnit.DAYS),
                schedule);
        Participant creator = new Participant(ParticipantId.newId(), "Alice", Instant.now());
        return GroupBooking.reconstitute(
                id,
                trip.id(),
                trip.minParticipants(),
                trip.maxParticipants(),
                trip.bookingDeadline(),
                schedule,
                com.agencyvoyage.domain.booking.GroupBookingStatus.OPEN,
                List.of(creator));
    }
}
