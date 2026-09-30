package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.EmailAlreadyRegisteredException;
import com.agencyvoyage.application.port.in.AuthResult;
import com.agencyvoyage.application.port.in.RegisterUserCommand;
import com.agencyvoyage.application.port.in.RegisterUserUseCase;
import com.agencyvoyage.application.port.out.PasswordHasher;
import com.agencyvoyage.application.port.out.TokenIssuer;
import com.agencyvoyage.application.port.out.UserRepository;
import com.agencyvoyage.domain.user.User;
import java.util.Objects;

public final class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public RegisterUserService(UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
        this.tokenIssuer = Objects.requireNonNull(tokenIssuer, "tokenIssuer must not be null");
    }

    @Override
    public AuthResult register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyRegisteredException(command.email());
        }

        String hashedPassword = passwordHasher.hash(command.rawPassword());
        User user = userRepository.createAccount(command.email(), hashedPassword, command.displayName());

        return new AuthResult(user, tokenIssuer.issueToken(user));
    }
}
