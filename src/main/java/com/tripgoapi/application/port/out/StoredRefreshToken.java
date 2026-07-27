package com.tripgoapi.application.port.out;

import java.time.OffsetDateTime;

public record StoredRefreshToken(Long id, Long userId, OffsetDateTime expiresAt, boolean revoked) {
}
