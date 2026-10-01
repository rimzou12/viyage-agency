package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(UserId id);

    Optional<UserAccount> findAccountByEmail(String email);

    boolean existsByEmail(String email);

    /** Most callers register a regular (non-admin) user. */
    default User createAccount(String email, String hashedPassword, String displayName) {
        return createAccount(email, hashedPassword, displayName, false);
    }

    User createAccount(String email, String hashedPassword, String displayName, boolean isAdmin);
}
