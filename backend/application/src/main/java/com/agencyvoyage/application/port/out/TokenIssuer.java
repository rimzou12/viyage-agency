package com.agencyvoyage.application.port.out;

import com.agencyvoyage.domain.user.User;

public interface TokenIssuer {

    /** A signed, self-contained token identifying {@code user} - opaque to the application layer. */
    String issueToken(User user);
}
