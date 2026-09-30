package com.agencyvoyage.application.port.in;

public record RegisterUserCommand(String email, String rawPassword, String displayName) {

    public RegisterUserCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("password must be at least 8 characters");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
    }
}
