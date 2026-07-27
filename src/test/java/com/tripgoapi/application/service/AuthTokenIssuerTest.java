package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.AuthToken;
import com.tripgoapi.application.port.out.AccessToken;
import com.tripgoapi.application.port.out.GeneratedRefreshToken;
import com.tripgoapi.application.port.out.RefreshTokenGeneratorPort;
import com.tripgoapi.application.port.out.RefreshTokenRepositoryInterface;
import com.tripgoapi.application.port.out.TokenProviderInterface;
import com.tripgoapi.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthTokenIssuerTest {

    @Mock
    private TokenProviderInterface tokenProvider;
    @Mock
    private RefreshTokenGeneratorPort refreshTokenGenerator;
    @Mock
    private RefreshTokenRepositoryInterface refreshTokenRepository;

    @Test
    void issuesAccessAndRefreshToken_andPersistsOnlyTheRefreshTokenHash() {
        AuthTokenIssuer issuer = new AuthTokenIssuer(tokenProvider, refreshTokenGenerator, refreshTokenRepository);
        AccessToken accessToken = new AccessToken("jwt-value", "Bearer", 3600);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(14);
        GeneratedRefreshToken refreshToken = new GeneratedRefreshToken("raw-refresh", "hashed-refresh", expiresAt);

        when(tokenProvider.generateToken(1L, "jane@example.com", Role.USER)).thenReturn(accessToken);
        when(refreshTokenGenerator.generate()).thenReturn(refreshToken);

        AuthToken result = issuer.issue(1L, "jane@example.com", Role.USER);

        assertThat(result).isEqualTo(new AuthToken("jwt-value", "raw-refresh", "Bearer", 3600));
        verify(refreshTokenRepository).save(1L, "hashed-refresh", expiresAt);
    }
}
