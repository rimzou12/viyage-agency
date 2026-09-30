package com.agencyvoyage.application.exception;

public final class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException(String email) {
        super("An account already exists for " + email);
    }
}
