package com.tripgoapi.application.port.out;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface RefreshTokenRepositoryInterface {

    void save(Long userId, String tokenHash, OffsetDateTime expiresAt);

    Optional<StoredRefreshToken> findByTokenHash(String tokenHash);

    /**
     * Atomically revokes the token only if it is not already revoked.
     * @return true if this call revoked it, false if it was already revoked (or unknown)
     */
    boolean revokeIfActive(String tokenHash);

    void revokeAllForUser(Long userId);
}
