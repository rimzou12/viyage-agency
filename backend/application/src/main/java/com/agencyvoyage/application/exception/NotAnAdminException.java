package com.agencyvoyage.application.exception;

public final class NotAnAdminException extends RuntimeException {

    public NotAnAdminException() {
        super("This action is restricted to admins");
    }
}
