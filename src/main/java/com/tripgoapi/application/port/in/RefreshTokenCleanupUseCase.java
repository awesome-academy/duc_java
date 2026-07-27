package com.tripgoapi.application.port.in;

public interface RefreshTokenCleanupUseCase {

    /**
     * Purges expired and stale-revoked refresh token rows.
     * @return number of rows deleted
     */
    int cleanupExpiredAndStaleRevokedTokens();
}
