package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.AuthToken;
import com.tripgoapi.application.port.out.RefreshTokenGeneratorPort;
import com.tripgoapi.application.port.out.RefreshTokenRepositoryInterface;
import com.tripgoapi.application.port.out.StoredRefreshToken;
import com.tripgoapi.application.port.out.UserRepositoryInterface;
import com.tripgoapi.domain.exception.InvalidRefreshTokenException;
import com.tripgoapi.domain.model.Role;
import com.tripgoapi.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepositoryInterface refreshTokenRepository;
    @Mock
    private RefreshTokenGeneratorPort refreshTokenGenerator;
    @Mock
    private UserRepositoryInterface userRepository;
    @Mock
    private AuthTokenIssuer authTokenIssuer;
    @Mock
    private RefreshTokenSecurityIncidentHandler securityIncidentHandler;

    private RefreshTokenService service;

    private RefreshTokenService newService() {
        return new RefreshTokenService(
                refreshTokenRepository, refreshTokenGenerator, userRepository, authTokenIssuer, securityIncidentHandler);
    }

    @Test
    void unknownTokenHash_throwsInvalidRefreshToken() {
        service = newService();
        when(refreshTokenGenerator.hash("raw")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never()).revokeIfActive(any());
        verifyNoInteractions(securityIncidentHandler, authTokenIssuer);
    }

    @Test
    void expiredToken_throwsInvalidRefreshToken_withoutTouchingRevokeOrIncidentHandler() {
        service = newService();
        StoredRefreshToken stored = new StoredRefreshToken(1L, 10L, OffsetDateTime.now().minusMinutes(1), false);
        when(refreshTokenGenerator.hash("raw")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> service.refresh("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never()).revokeIfActive(any());
        verifyNoInteractions(securityIncidentHandler, authTokenIssuer);
    }

    @Test
    void losingTheAtomicRevokeRace_triggersSecurityIncidentAndRejects() {
        // Regression test for the TOCTOU fix: revokeIfActive()==false means this call did NOT
        // win the atomic "revoke while not already revoked" race — either the token was
        // already rotated-out, or another concurrent request redeemed it first. Either way it
        // must be treated as reuse/theft, never silently ignored.
        service = newService();
        StoredRefreshToken stored = new StoredRefreshToken(1L, 10L, OffsetDateTime.now().plusMinutes(5), false);
        when(refreshTokenGenerator.hash("raw")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.revokeIfActive("hash")).thenReturn(false);

        assertThatThrownBy(() -> service.refresh("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        verify(securityIncidentHandler).revokeAllSessions(10L);
        verifyNoInteractions(authTokenIssuer);
    }

    @Test
    void winningTheAtomicRevokeRace_issuesNewTokenPair() {
        service = newService();
        StoredRefreshToken stored = new StoredRefreshToken(1L, 10L, OffsetDateTime.now().plusMinutes(5), false);
        User user = new User(10L, "Jane", "jane@example.com", null, Role.USER);
        AuthToken issued = new AuthToken("access", "refresh", "Bearer", 3600);

        when(refreshTokenGenerator.hash("raw")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.revokeIfActive("hash")).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(authTokenIssuer.issue(10L, "jane@example.com", Role.USER)).thenReturn(issued);

        AuthToken result = service.refresh("raw");

        assertThat(result).isSameAs(issued);
        verifyNoInteractions(securityIncidentHandler);
    }

    @Test
    void userDeletedAfterTokenIssued_throwsInvalidRefreshToken() {
        service = newService();
        StoredRefreshToken stored = new StoredRefreshToken(1L, 10L, OffsetDateTime.now().plusMinutes(5), false);
        when(refreshTokenGenerator.hash("raw")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.revokeIfActive("hash")).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        verifyNoInteractions(authTokenIssuer);
    }
}
