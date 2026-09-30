package com.agencyvoyage.application.port.in;

import com.agencyvoyage.application.exception.InvalidCredentialsException;

public interface LoginUseCase {

    /** @throws InvalidCredentialsException if the email is unknown or the password does not match */
    AuthResult login(LoginCommand command);
}
