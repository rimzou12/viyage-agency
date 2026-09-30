package com.agencyvoyage.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.agencyvoyage.application.exception.TripNotFoundException;
import com.agencyvoyage.application.port.in.GetTripUseCase;
import com.agencyvoyage.application.port.in.ListTripsUseCase;
import com.agencyvoyage.domain.trip.PricingSchedule;
import com.agencyvoyage.domain.trip.Trip;
import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.infrastructure.security.JwtTokenParser;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(TripController.class)
class TripControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private ListTripsUseCase listTripsUseCase;

    @MockitoBean
    private GetTripUseCase getTripUseCase;

    /**
     * Not used by TripController, but JwtAuthenticationFilter is a servlet Filter, so
     * @WebMvcTest's scanning constructs it regardless of which controller is under
     * test - it needs this dependency satisfied to build the context at all.
     */
    @MockitoBean
    private JwtTokenParser jwtTokenParser;

    @Test
    void listsTripsAsJson() {
        when(listTripsUseCase.listTrips()).thenReturn(List.of(trip()));

        assertThat(mvc.get().uri("/api/trips"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].destination")
                .isEqualTo("Bali");
    }

    @Test
    void returns404WhenTheTripDoesNotExist() {
        TripId unknownId = TripId.newId();
        when(getTripUseCase.getTrip(unknownId)).thenThrow(new TripNotFoundException(unknownId));

        assertThat(mvc.get().uri("/api/trips/" + unknownId)).hasStatus(404);
    }

    private static Trip trip() {
        PricingSchedule schedule = PricingSchedule.of(new BigDecimal("1000"), List.of(), 5);
        return new Trip(
                TripId.newId(),
                "Bali",
                "desc",
                LocalDate.of(2027, 6, 10),
                LocalDate.of(2027, 6, 20),
                2,
                5,
                Instant.now().plus(30, ChronoUnit.DAYS),
                schedule);
    }
}
