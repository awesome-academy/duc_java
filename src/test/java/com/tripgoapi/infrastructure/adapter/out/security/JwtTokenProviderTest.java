package com.tripgoapi.infrastructure.adapter.out.security;

import com.tripgoapi.application.port.out.AccessToken;
import com.tripgoapi.application.port.out.AuthenticatedPrincipal;
import com.tripgoapi.domain.model.Role;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    // 32+ bytes, as required for HS256 (HMAC-SHA256) keys.
    private static final String VALID_SECRET = "unit-test-secret-key-min-32-bytes-long!!";

    @Test
    void generateThenParse_roundTripsClaims() {
        JwtTokenProvider provider = new JwtTokenProvider(VALID_SECRET, 3600);

        AccessToken token = provider.generateToken(1L, "jane@example.com", Role.USER);
        Optional<AuthenticatedPrincipal> parsed = provider.parseToken(token.value());

        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(token.expiresInSeconds()).isEqualTo(3600);
        assertThat(parsed).contains(new AuthenticatedPrincipal(1L, "jane@example.com", Role.USER));
    }

    @Test
    void tamperedToken_failsToParse() {
        JwtTokenProvider provider = new JwtTokenProvider(VALID_SECRET, 3600);
        AccessToken token = provider.generateToken(1L, "jane@example.com", Role.USER);

        assertThat(provider.parseToken(token.value() + "tampered")).isEmpty();
    }

    @Test
    void tokenSignedWithDifferentSecret_isRejected() {
        JwtTokenProvider issuer = new JwtTokenProvider(VALID_SECRET, 3600);
        JwtTokenProvider verifier = new JwtTokenProvider("another-unit-test-secret-key-min-32-bytes", 3600);
        AccessToken token = issuer.generateToken(1L, "jane@example.com", Role.USER);

        assertThat(verifier.parseToken(token.value())).isEmpty();
    }

    @Test
    void secretShorterThan32Bytes_isRejectedAtConstruction() {
        // Regression guard for the "shipped a public JWT default secret" finding: any secret
        // that's too weak for HS256 must fail fast at startup, not silently sign tokens.
        assertThatThrownBy(() -> new JwtTokenProvider("too-short-secret", 3600))
                .isInstanceOf(WeakKeyException.class);
    }
}
