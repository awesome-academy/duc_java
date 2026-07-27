package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.AuthToken;
import com.tripgoapi.application.port.in.RefreshTokenUseCase;
import com.tripgoapi.application.port.out.RefreshTokenGeneratorPort;
import com.tripgoapi.application.port.out.RefreshTokenRepositoryInterface;
import com.tripgoapi.application.port.out.StoredRefreshToken;
import com.tripgoapi.application.port.out.UserRepositoryInterface;
import com.tripgoapi.domain.exception.InvalidRefreshTokenException;
import com.tripgoapi.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final RefreshTokenRepositoryInterface refreshTokenRepository;
    private final RefreshTokenGeneratorPort refreshTokenGenerator;
    private final UserRepositoryInterface userRepository;
    private final AuthTokenIssuer authTokenIssuer;
    private final RefreshTokenSecurityIncidentHandler securityIncidentHandler;

    @Override
    @Transactional
    public AuthToken refresh(String rawRefreshToken) {
        String hash = refreshTokenGenerator.hash(rawRefreshToken);
        StoredRefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (stored.expiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidRefreshTokenException();
        }

        // Atomic conditional revoke (single UPDATE ... WHERE revoked = false at the DB level):
        // only one concurrent caller can ever win this for a given token. Losing means the
        // token was already revoked — either normal reuse of a rotated-out token, or another
        // request redeemed it a moment earlier — either way treat it as theft and kill all
        // sessions for this user. revokeAllSessions runs in its own REQUIRES_NEW transaction
        // so it isn't rolled back by the exception thrown right after.
        if (!refreshTokenRepository.revokeIfActive(hash)) {
            securityIncidentHandler.revokeAllSessions(stored.userId());
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(stored.userId())
                .orElseThrow(InvalidRefreshTokenException::new);

        return authTokenIssuer.issue(user.id(), user.email(), user.role());
    }
}
