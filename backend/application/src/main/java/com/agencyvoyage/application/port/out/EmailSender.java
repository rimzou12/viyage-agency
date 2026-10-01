package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.booking.GroupBookingId;
import java.math.BigDecimal;

public interface EmailSender {

    void sendGroupBookingJoined(
            String toEmail,
            String recipientName,
            GroupBookingId bookingId,
            int participantCount,
            BigDecimal pricePerSeat);

    void sendHotelReservationRequested(
            String toEmail, String recipientName, GroupBookingId bookingId, String reference);

    void sendHotelReservationConfirmed(
            String toEmail, String recipientName, GroupBookingId bookingId, String reference);
}
