package com.agencyvoyage.infrastructure.security;

import com.agencyvoyage.domain.user.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Decodes and verifies a JWT issued by {@link JwtTokenIssuer}. Not an application
 * out-port: parsing an incoming Authorization header is a request-handling concern the
 * web layer owns, not a business operation - this is just the reusable crypto bit.
 */
@Component
public class JwtTokenParser {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenParser.class);

    private final SecretKey key;

    public JwtTokenParser(@Value("${agency-voyage.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Empty if the token is missing, malformed, expired, or has a bad signature. */
    public Optional<ParsedToken> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(new ParsedToken(
                    UserId.of(claims.getSubject()),
                    claims.get("email", String.class),
                    claims.get("displayName", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Rejecting an invalid JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
