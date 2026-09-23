package com.myrecipe.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtTokenServiceTest {
    private static final String TEST_SECRET = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    @Test
    public void generateTokenCreatesValidTokenForUser() {
        JwtTokenService tokenService = tokenService(60);
        UserDetails user = user("user@mail.com", true);

        String token = tokenService.generateToken(user);

        assertThat(tokenService.getSubject(token)).isEqualTo("user@mail.com");
        assertThat(tokenService.isTokenValid(token, user)).isTrue();
        assertThat(tokenService.getExpiration(token)).isAfter(Instant.parse("2026-09-23T12:00:00Z"));
    }

    @Test
    public void expiredTokenIsRejected() {
        JwtTokenService tokenService = tokenService(-1);
        String token = tokenService.generateToken(user("user@mail.com", true));

        assertThatThrownBy(() -> tokenService.getSubject(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    public void malformedTokenIsRejected() {
        JwtTokenService tokenService = tokenService(60);

        assertThatThrownBy(() -> tokenService.getSubject("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    public void shortSecretIsRejected() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("c2hvcnQ=");
        properties.setIssuer("myrecipes-test");
        properties.setExpirationMinutes(60);

        assertThatThrownBy(() -> new JwtTokenService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret must be at least 256 bits");
    }

    private JwtTokenService tokenService(long expirationMinutes) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(TEST_SECRET);
        properties.setIssuer("myrecipes-test");
        properties.setExpirationMinutes(expirationMinutes);
        return new JwtTokenService(
                properties,
                Clock.fixed(Instant.parse("2026-09-23T12:00:00Z"), ZoneOffset.UTC));
    }

    private UserDetails user(String email, boolean enabled) {
        return new User(
                email,
                "password",
                enabled,
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
