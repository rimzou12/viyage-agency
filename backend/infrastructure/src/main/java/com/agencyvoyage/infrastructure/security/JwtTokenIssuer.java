package com.agencyvoyage.infrastructure.security;

import com.agencyvoyage.application.port.out.TokenIssuer;
import com.agencyvoyage.domain.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenIssuer(
            @Value("${agency-voyage.jwt.secret}") String secret,
            @Value("${agency-voyage.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String issueToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.id().toString())
                .claim("email", user.email())
                .claim("displayName", user.displayName())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }
}
