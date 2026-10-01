package com.agencyvoyage.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.domain.user.UserId;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class JwtRoundTripTest {

    private static final String SECRET = "test-secret-key-at-least-32-bytes-long!!";

    private final JwtTokenIssuer issuer = new JwtTokenIssuer(SECRET, 60_000);
    private final JwtTokenParser parser = new JwtTokenParser(SECRET);

    @Test
    void aTokenIssuedForAUserParsesBackToTheSameClaims() {
        User user = new User(UserId.newId(), "alice@example.com", "Alice");

        String token = issuer.issueToken(user);
        Optional<ParsedToken> parsed = parser.parse(token);

        assertThat(parsed).isPresent();
        assertThat(parsed.get().userId()).isEqualTo(user.id());
        assertThat(parsed.get().email()).isEqualTo("alice@example.com");
        assertThat(parsed.get().displayName()).isEqualTo("Alice");
        assertThat(parsed.get().isAdmin()).isFalse();
    }

    @Test
    void anAdminUsersTokenParsesBackAsAdmin() {
        User admin = new User(UserId.newId(), "admin@example.com", "Admin", true);

        String token = issuer.issueToken(admin);
        Optional<ParsedToken> parsed = parser.parse(token);

        assertThat(parsed).isPresent();
        assertThat(parsed.get().isAdmin()).isTrue();
    }

    @Test
    void aTamperedTokenDoesNotParse() {
        User user = new User(UserId.newId(), "alice@example.com", "Alice");
        String token = issuer.issueToken(user);

        assertThat(parser.parse(token + "tampered")).isEmpty();
    }

    @Test
    void aTokenSignedWithADifferentSecretDoesNotParse() {
        User user = new User(UserId.newId(), "alice@example.com", "Alice");
        JwtTokenIssuer otherIssuer = new JwtTokenIssuer("a-completely-different-secret-value!!!!", 60_000);
        String token = otherIssuer.issueToken(user);

        assertThat(parser.parse(token)).isEmpty();
    }

    @Test
    void anExpiredTokenDoesNotParse() {
        User user = new User(UserId.newId(), "alice@example.com", "Alice");
        JwtTokenIssuer expiringIssuer = new JwtTokenIssuer(SECRET, -1_000);
        String token = expiringIssuer.issueToken(user);

        assertThat(parser.parse(token)).isEmpty();
    }
}
