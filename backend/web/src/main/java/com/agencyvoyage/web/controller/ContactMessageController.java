package com.agencyvoyage.web.controller;

import com.agencyvoyage.application.port.in.ListContactMessagesUseCase;
import com.agencyvoyage.application.port.in.SendContactMessageCommand;
import com.agencyvoyage.application.port.in.SendContactMessageUseCase;
import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.web.dto.ContactMessageResponse;
import com.agencyvoyage.web.dto.SendContactMessageRequest;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactMessageController {

    private final SendContactMessageUseCase sendContactMessageUseCase;
    private final ListContactMessagesUseCase listContactMessagesUseCase;

    public ContactMessageController(
            SendContactMessageUseCase sendContactMessageUseCase,
            ListContactMessagesUseCase listContactMessagesUseCase) {
        this.sendContactMessageUseCase = Objects.requireNonNull(sendContactMessageUseCase);
        this.listContactMessagesUseCase = Objects.requireNonNull(listContactMessagesUseCase);
    }

    @PostMapping("/api/contact-messages")
    public ResponseEntity<ContactMessageResponse> sendMessage(
            @RequestBody SendContactMessageRequest request, @AuthenticationPrincipal User currentUser) {
        ContactMessage message = sendContactMessageUseCase.sendMessage(
                new SendContactMessageCommand(currentUser, request.subject(), request.message()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ContactMessageResponse.from(message));
    }

    @GetMapping("/api/contact-messages")
    public List<ContactMessageResponse> listMessages() {
        return listContactMessagesUseCase.listMessages().stream()
                .map(ContactMessageResponse::from)
                .toList();
    }
}
