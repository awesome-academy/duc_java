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

    /**
     * Purges rows that no longer serve any purpose: tokens past {@code now} (dead regardless
     * of revoked state), and revoked tokens created before {@code revokedRetentionBefore}
     * (kept briefly for audit/incident review, then purged even if not yet expired).
     * @return number of rows deleted
     */
    int deleteExpiredOrStaleRevoked(OffsetDateTime now, OffsetDateTime revokedRetentionBefore);
}
