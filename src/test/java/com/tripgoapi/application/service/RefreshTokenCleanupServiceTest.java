package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.RefreshTokenRepositoryInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupServiceTest {

    @Mock
    private RefreshTokenRepositoryInterface refreshTokenRepository;

    @Test
    void cleanup_deletesExpiredTokens_andRevokedTokensOlderThanSevenDays() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC);
        RefreshTokenCleanupService service = new RefreshTokenCleanupService(refreshTokenRepository, fixedClock);
        OffsetDateTime now = OffsetDateTime.now(fixedClock);
        OffsetDateTime expectedRevokedRetentionBefore = now.minusDays(7);
        when(refreshTokenRepository.deleteExpiredOrStaleRevoked(now, expectedRevokedRetentionBefore)).thenReturn(4);

        int deleted = service.cleanupExpiredAndStaleRevokedTokens();

        assertThat(deleted).isEqualTo(4);
        verify(refreshTokenRepository).deleteExpiredOrStaleRevoked(now, expectedRevokedRetentionBefore);
    }
}
