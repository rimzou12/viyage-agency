package com.agencyvoyage.web.controller;

import com.agencyvoyage.application.port.in.GetConversationCommand;
import com.agencyvoyage.application.port.in.GetConversationUseCase;
import com.agencyvoyage.application.port.in.ListContactMessagesUseCase;
import com.agencyvoyage.application.port.in.ReplyToConversationCommand;
import com.agencyvoyage.application.port.in.ReplyToConversationUseCase;
import com.agencyvoyage.application.port.in.SendContactMessageCommand;
import com.agencyvoyage.application.port.in.SendContactMessageUseCase;
import com.agencyvoyage.domain.support.ContactMessage;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import com.agencyvoyage.web.dto.ContactMessageResponse;
import com.agencyvoyage.web.dto.ReplyToConversationRequest;
import com.agencyvoyage.web.dto.SendContactMessageRequest;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactMessageController {

    private final SendContactMessageUseCase sendContactMessageUseCase;
    private final ReplyToConversationUseCase replyToConversationUseCase;
    private final GetConversationUseCase getConversationUseCase;
    private final ListContactMessagesUseCase listContactMessagesUseCase;

    public ContactMessageController(
            SendContactMessageUseCase sendContactMessageUseCase,
            ReplyToConversationUseCase replyToConversationUseCase,
            GetConversationUseCase getConversationUseCase,
            ListContactMessagesUseCase listContactMessagesUseCase) {
        this.sendContactMessageUseCase = Objects.requireNonNull(sendContactMessageUseCase);
        this.replyToConversationUseCase = Objects.requireNonNull(replyToConversationUseCase);
        this.getConversationUseCase = Objects.requireNonNull(getConversationUseCase);
        this.listContactMessagesUseCase = Objects.requireNonNull(listContactMessagesUseCase);
    }

    @PostMapping("/api/contact-messages")
    public ResponseEntity<ContactMessageResponse> sendMessage(
            @RequestBody SendContactMessageRequest request, @AuthenticationPrincipal User currentUser) {
        ContactMessage message =
                sendContactMessageUseCase.sendMessage(new SendContactMessageCommand(currentUser, request.message()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ContactMessageResponse.from(message));
    }

    @PostMapping("/api/contact-messages/reply")
    public ResponseEntity<ContactMessageResponse> reply(
            @RequestBody ReplyToConversationRequest request, @AuthenticationPrincipal User currentUser) {
        ContactMessage message = replyToConversationUseCase.reply(new ReplyToConversationCommand(
                UserId.of(request.conversationUserId()), currentUser, request.message()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ContactMessageResponse.from(message));
    }

    @GetMapping("/api/contact-messages/conversations/{userId}")
    public List<ContactMessageResponse> getConversation(
            @PathVariable String userId, @AuthenticationPrincipal User currentUser) {
        return getConversationUseCase
                .getConversation(new GetConversationCommand(UserId.of(userId), currentUser))
                .stream()
                .map(ContactMessageResponse::from)
                .toList();
    }

    @GetMapping("/api/contact-messages")
    public List<ContactMessageResponse> listMessages(@AuthenticationPrincipal User currentUser) {
        return listContactMessagesUseCase.listMessages(currentUser).stream()
                .map(ContactMessageResponse::from)
                .toList();
    }
}
