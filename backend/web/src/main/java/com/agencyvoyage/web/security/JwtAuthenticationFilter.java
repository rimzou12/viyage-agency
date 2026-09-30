package com.agencyvoyage.web.security;

import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.infrastructure.security.JwtTokenParser;
import com.agencyvoyage.infrastructure.security.ParsedToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Populates the security context from a Bearer token when one is present, on every
 * request - including ones that don't require authentication, so a GET on a public
 * endpoint can still tell "this caller happens to be logged in as X". Never rejects a
 * request itself; {@code SecurityConfig}'s authorization rules decide what actually
 * requires an authenticated principal.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenParser tokenParser;

    public JwtAuthenticationFilter(JwtTokenParser tokenParser) {
        this.tokenParser = Objects.requireNonNull(tokenParser, "tokenParser must not be null");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        extractBearerToken(request)
                .flatMap(tokenParser::parse)
                .ifPresent(this::authenticate);

        chain.doFilter(request, response);
    }

    private void authenticate(ParsedToken parsed) {
        User user = new User(parsed.userId(), parsed.email(), parsed.displayName());
        var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static Optional<String> extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring("Bearer ".length()));
        }
        return Optional.empty();
    }
}
