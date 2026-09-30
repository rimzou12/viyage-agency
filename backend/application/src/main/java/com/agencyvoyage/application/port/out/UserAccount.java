package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.user.User;

/** A user's identity plus the credential material needed to verify a login attempt. */
public record UserAccount(User user, String hashedPassword) {
}
