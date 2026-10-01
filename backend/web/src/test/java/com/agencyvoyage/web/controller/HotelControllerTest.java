package com.agencyvoyage.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import com.agencyvoyage.application.exception.NotAnAdminException;
import com.agencyvoyage.application.port.in.AddHotelCommand;
import com.agencyvoyage.application.port.in.AddHotelUseCase;
import com.agencyvoyage.application.port.in.ListHotelsForTripUseCase;
import com.agencyvoyage.domain.hotel.Hotel;
import com.agencyvoyage.domain.hotel.HotelId;
import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import com.agencyvoyage.infrastructure.security.JwtTokenParser;
import com.agencyvoyage.web.config.CorsConfig;
import com.agencyvoyage.web.security.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(HotelController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class HotelControllerTest {

    private static final User ADMIN = new User(UserId.newId(), "admin@example.com", "Admin", true);
    private static final User ALICE = new User(UserId.newId(), "alice@example.com", "Alice");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private AddHotelUseCase addHotelUseCase;

    @MockitoBean
    private ListHotelsForTripUseCase listHotelsForTripUseCase;

    /**
     * Not used by HotelController, but JwtAuthenticationFilter is a servlet Filter, so
     * @WebMvcTest's scanning constructs it regardless of which controller is under
     * test - it needs this dependency satisfied to build the context at all.
     */
    @MockitoBean
    private JwtTokenParser jwtTokenParser;

    @Test
    void anAdminCanAddAHotelReturning201() {
        TripId tripId = TripId.newId();
        Hotel hotel = new Hotel(HotelId.newId(), tripId, "Ubud Retreat", "Jungle views", List.of("https://x/a.jpg"));
        when(addHotelUseCase.addHotel(any(AddHotelCommand.class))).thenReturn(hotel);

        assertThat(mvc.post()
                        .uri("/api/trips/" + tripId + "/hotels")
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ubud Retreat\",\"description\":\"Jungle views\",\"photoUrls\":[\"https://x/a.jpg\"]}"))
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.name")
                .isEqualTo("Ubud Retreat");
    }

    @Test
    void returns403WhenANonAdminTriesToAddAHotel() {
        TripId tripId = TripId.newId();
        when(addHotelUseCase.addHotel(any(AddHotelCommand.class))).thenThrow(new NotAnAdminException());

        assertThat(mvc.post()
                        .uri("/api/trips/" + tripId + "/hotels")
                        .with(asAlice())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ubud Retreat\",\"description\":\"Jungle views\",\"photoUrls\":[]}"))
                .hasStatus(403);
    }

    @Test
    void returns401WhenAddingAHotelWithoutAuthentication() {
        TripId tripId = TripId.newId();

        assertThat(mvc.post()
                        .uri("/api/trips/" + tripId + "/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ubud Retreat\",\"description\":\"Jungle views\",\"photoUrls\":[]}"))
                .hasStatus(401);
    }

    @Test
    void listHotelsReturnsThemAsJsonWithoutRequiringAuthentication() {
        TripId tripId = TripId.newId();
        Hotel hotel = new Hotel(HotelId.newId(), tripId, "Ubud Retreat", "Jungle views", List.of());
        when(listHotelsForTripUseCase.listHotels(tripId)).thenReturn(List.of(hotel));

        assertThat(mvc.get().uri("/api/trips/" + tripId + "/hotels"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("Ubud Retreat");
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor asAdmin() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(ADMIN, null, List.of());
        return authentication(authentication);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor asAlice() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(ALICE, null, List.of());
        return authentication(authentication);
    }
}
