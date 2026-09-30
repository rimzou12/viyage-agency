package com.agencyvoyage.web.controller;

import com.agencyvoyage.application.port.in.LoginCommand;
import com.agencyvoyage.application.port.in.LoginUseCase;
import com.agencyvoyage.application.port.in.RegisterUserCommand;
import com.agencyvoyage.application.port.in.RegisterUserUseCase;
import com.agencyvoyage.web.dto.AuthResponse;
import com.agencyvoyage.web.dto.LoginRequest;
import com.agencyvoyage.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUseCase loginUseCase) {
        this.registerUserUseCase = Objects.requireNonNull(registerUserUseCase);
        this.loginUseCase = Objects.requireNonNull(loginUseCase);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        var result = registerUserUseCase.register(
                new RegisterUserCommand(request.email(), request.password(), request.displayName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.login(new LoginCommand(request.email(), request.password()));
        return AuthResponse.from(result);
    }
}
