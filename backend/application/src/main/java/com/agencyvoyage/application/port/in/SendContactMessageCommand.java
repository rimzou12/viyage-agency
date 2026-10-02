package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.user.User;
import java.util.Objects;

public record SendContactMessageCommand(User author, String message) {

    public SendContactMessageCommand {
        Objects.requireNonNull(author, "author must not be null");
    }
}
