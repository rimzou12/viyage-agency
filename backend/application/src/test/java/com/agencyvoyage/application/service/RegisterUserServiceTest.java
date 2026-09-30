package com.agencyvoyage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.agencyvoyage.application.exception.EmailAlreadyRegisteredException;
import com.agencyvoyage.application.port.in.AuthResult;
import com.agencyvoyage.application.port.in.RegisterUserCommand;
import com.agencyvoyage.application.port.out.PasswordHasher;
import com.agencyvoyage.application.port.out.TokenIssuer;
import com.agencyvoyage.application.port.out.UserRepository;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenIssuer tokenIssuer;

    private RegisterUserService service;

    @Test
    void createsAnAccountHashesThePasswordAndIssuesAToken() {
        service = new RegisterUserService(userRepository, passwordHasher, tokenIssuer);
        User created = new User(UserId.newId(), "alice@example.com", "Alice");
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordHasher.hash("password123")).thenReturn("hashed");
        when(userRepository.createAccount("alice@example.com", "hashed", "Alice")).thenReturn(created);
        when(tokenIssuer.issueToken(created)).thenReturn("jwt-token");

        AuthResult result = service.register(new RegisterUserCommand("alice@example.com", "password123", "Alice"));

        assertThat(result.user()).isEqualTo(created);
        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void throwsWhenTheEmailIsAlreadyRegistered() {
        service = new RegisterUserService(userRepository, passwordHasher, tokenIssuer);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                        new RegisterUserCommand("alice@example.com", "password123", "Alice")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }
}
