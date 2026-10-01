package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.booking.GroupBookingId;

public interface EmailSender {

    void sendHotelReservationRequested(
            String toEmail, String recipientName, GroupBookingId bookingId, String reference);

    void sendHotelReservationConfirmed(
            String toEmail, String recipientName, GroupBookingId bookingId, String reference);
}
