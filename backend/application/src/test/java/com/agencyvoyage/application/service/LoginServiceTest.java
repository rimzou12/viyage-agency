package com.agencyvoyage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.agencyvoyage.application.exception.InvalidCredentialsException;
import com.agencyvoyage.application.port.in.AuthResult;
import com.agencyvoyage.application.port.in.LoginCommand;
import com.agencyvoyage.application.port.out.PasswordHasher;
import com.agencyvoyage.application.port.out.TokenIssuer;
import com.agencyvoyage.application.port.out.UserAccount;
import com.agencyvoyage.application.port.out.UserRepository;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenIssuer tokenIssuer;

    private LoginService service;

    @Test
    void logsInWithCorrectCredentialsAndIssuesAToken() {
        service = new LoginService(userRepository, passwordHasher, tokenIssuer);
        User user = new User(UserId.newId(), "alice@example.com", "Alice");
        UserAccount account = new UserAccount(user, "hashed");
        when(userRepository.findAccountByEmail("alice@example.com")).thenReturn(Optional.of(account));
        when(passwordHasher.matches("password123", "hashed")).thenReturn(true);
        when(tokenIssuer.issueToken(user)).thenReturn("jwt-token");

        AuthResult result = service.login(new LoginCommand("alice@example.com", "password123"));

        assertThat(result.user()).isEqualTo(user);
        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void throwsWhenTheEmailIsUnknown() {
        service = new LoginService(userRepository, passwordHasher, tokenIssuer);
        when(userRepository.findAccountByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginCommand("nobody@example.com", "password123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void throwsWhenThePasswordDoesNotMatch() {
        service = new LoginService(userRepository, passwordHasher, tokenIssuer);
        User user = new User(UserId.newId(), "alice@example.com", "Alice");
        UserAccount account = new UserAccount(user, "hashed");
        when(userRepository.findAccountByEmail("alice@example.com")).thenReturn(Optional.of(account));
        when(passwordHasher.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginCommand("alice@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
