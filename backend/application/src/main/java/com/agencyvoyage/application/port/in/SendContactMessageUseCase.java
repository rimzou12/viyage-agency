package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.support.ContactMessage;

public interface SendContactMessageUseCase {

    ContactMessage sendMessage(SendContactMessageCommand command);
}
