package com.agencyvoyage.infrastructure.email;

import com.agencyvoyage.application.port.out.EmailSender;
import com.agencyvoyage.domain.booking.GroupBookingId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stands in for a real email provider (SES, SendGrid, ...) in this MVP: logs what
 * would have been sent instead of actually sending it, the same simplification the
 * project already makes for Kafka notifications in {@code NotificationKafkaListener}.
 */
@Component
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void sendHotelReservationConfirmed(
            String toEmail, String recipientName, GroupBookingId bookingId, String reference) {
        log.info(
                "[EMAIL] To: {} <{}> - Your hotel for group booking {} is confirmed! Reference: {}",
                recipientName,
                toEmail,
                bookingId,
                reference);
    }
}
