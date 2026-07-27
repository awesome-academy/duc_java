package com.tripgoapi.application.port.out;

import java.time.OffsetDateTime;

public record GeneratedRefreshToken(String rawValue, String hash, OffsetDateTime expiresAt) {
}
