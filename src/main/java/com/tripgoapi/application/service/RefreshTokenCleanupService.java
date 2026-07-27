package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.RefreshTokenCleanupUseCase;
import com.tripgoapi.application.port.out.RefreshTokenRepositoryInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;

@Service
public class RefreshTokenCleanupService implements RefreshTokenCleanupUseCase {

    private static final Duration REVOKED_RETENTION = Duration.ofDays(7);

    private final RefreshTokenRepositoryInterface refreshTokenRepository;
    private final Clock clock;

    @Autowired
    public RefreshTokenCleanupService(RefreshTokenRepositoryInterface refreshTokenRepository) {
        this(refreshTokenRepository, Clock.systemUTC());
    }

    RefreshTokenCleanupService(RefreshTokenRepositoryInterface refreshTokenRepository, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public int cleanupExpiredAndStaleRevokedTokens() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        return refreshTokenRepository.deleteExpiredOrStaleRevoked(now, now.minus(REVOKED_RETENTION));
    }
}
