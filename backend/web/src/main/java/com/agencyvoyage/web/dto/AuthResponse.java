package com.agencyvoyage.web.dto;

import com.agencyvoyage.application.port.in.AuthResult;

public record AuthResponse(String token, UserResponse user) {

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.token(), UserResponse.from(result.user()));
    }
}
