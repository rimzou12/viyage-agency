package com.agencyvoyage.domain.user;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A registered account's identity. Deliberately carries no password or credential
 * material - that's an infrastructure/security concern (hashing, verification), not a
 * business concept, so it lives in the auth adapters instead.
 */
public record User(UserId id, String email, String displayName, boolean isAdmin) {

    private static final Pattern SIMPLE_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public User {
        Objects.requireNonNull(id, "id must not be null");
        if (email == null || !SIMPLE_EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("email must be a valid address, got " + email);
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
    }

    /** Most callers don't care about admin status - defaults to a regular (non-admin) user. */
    public User(UserId id, String email, String displayName) {
        this(id, email, displayName, false);
    }
}
