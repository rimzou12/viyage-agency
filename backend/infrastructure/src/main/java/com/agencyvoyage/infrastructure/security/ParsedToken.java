package com.agencyvoyage.infrastructure.security;

import com.agencyvoyage.domain.user.UserId;

public record ParsedToken(UserId userId, String email, String displayName, boolean isAdmin) {
}
