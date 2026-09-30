package com.agencyvoyage.application.port.in;

import com.agencyvoyage.application.exception.EmailAlreadyRegisteredException;

public interface RegisterUserUseCase {

    /** @throws EmailAlreadyRegisteredException if the email is already registered */
    AuthResult register(RegisterUserCommand command);
}
