package com.myrecipe.security;

import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private static final int MIN_HS256_SECRET_BYTES = 32;

    private final JwtProperties jwtProperties;
    private final Key signingKey;
    private final Clock clock;

    @Autowired
    public JwtTokenService(JwtProperties jwtProperties) {
        this(jwtProperties, Clock.systemUTC());
    }

    JwtTokenService(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
        this.signingKey = buildSigningKey(jwtProperties.getSecret());
    }

    public String generateToken(UserDetails userDetails) {
        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = issuedAt.plus(jwtProperties.getExpirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .setId(UUID.randomUUID().toString())
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String subject = getSubject(token);
        return subject.equals(userDetails.getUsername()) && userDetails.isEnabled();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .setClock(() -> Date.from(Instant.now(clock)))
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key buildSigningKey(String base64Secret) {
        if (base64Secret == null || base64Secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret must be configured");
        }

        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        if (keyBytes.length < MIN_HS256_SECRET_BYTES) {
            throw new IllegalStateException("JWT secret must be at least 256 bits");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
