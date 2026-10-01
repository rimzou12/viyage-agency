package com.agencyvoyage.domain.hotel;

import com.agencyvoyage.domain.trip.TripId;
import java.util.List;
import java.util.Objects;

/**
 * A hotel option an admin has curated for a trip, so travelers requesting a reservation
 * (see {@code GroupBooking.requestHotelReservation}) have something to pick from. Photos
 * are plain URLs - no upload/storage infrastructure, same simplification already used
 * for trip photos.
 */
public record Hotel(HotelId id, TripId tripId, String name, String description, List<String> photoUrls) {

    public Hotel {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tripId, "tripId must not be null");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        photoUrls = photoUrls == null ? List.of() : List.copyOf(photoUrls);
    }
}
