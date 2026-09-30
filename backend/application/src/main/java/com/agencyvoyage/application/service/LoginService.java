package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.InvalidCredentialsException;
import com.agencyvoyage.application.port.in.AuthResult;
import com.agencyvoyage.application.port.in.LoginCommand;
import com.agencyvoyage.application.port.in.LoginUseCase;
import com.agencyvoyage.application.port.out.PasswordHasher;
import com.agencyvoyage.application.port.out.TokenIssuer;
import com.agencyvoyage.application.port.out.UserAccount;
import com.agencyvoyage.application.port.out.UserRepository;
import java.util.Objects;

public final class LoginService implements LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public LoginService(UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
        this.tokenIssuer = Objects.requireNonNull(tokenIssuer, "tokenIssuer must not be null");
    }

    @Override
    public AuthResult login(LoginCommand command) {
        UserAccount account = userRepository.findAccountByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.rawPassword(), account.hashedPassword())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResult(account.user(), tokenIssuer.issueToken(account.user()));
    }
}
