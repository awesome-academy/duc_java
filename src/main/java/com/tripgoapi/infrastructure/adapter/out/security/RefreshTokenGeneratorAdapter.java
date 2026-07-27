package com.tripgoapi.infrastructure.adapter.out.security;

import com.tripgoapi.application.port.out.GeneratedRefreshToken;
import com.tripgoapi.application.port.out.RefreshTokenGeneratorPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class RefreshTokenGeneratorAdapter implements RefreshTokenGeneratorPort {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final long expirationSeconds;

    public RefreshTokenGeneratorAdapter(@Value("${tripgo.jwt.refresh-expiration-seconds}") long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public GeneratedRefreshToken generate() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);
        String rawValue = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        return new GeneratedRefreshToken(rawValue, hash(rawValue), OffsetDateTime.now().plusSeconds(expirationSeconds));
    }

    @Override
    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
