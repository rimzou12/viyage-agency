package com.agencyvoyage.application.port.in;

import com.agencyvoyage.domain.user.User;

public record AuthResult(User user, String token) {
}
