package com.tripgoapi.infrastructure.adapter.in.scheduler;

import com.tripgoapi.application.port.in.RefreshTokenCleanupUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupSchedulerTest {

    @Mock
    private RefreshTokenCleanupUseCase refreshTokenCleanupUseCase;

    @Test
    void cleanup_invokesUseCaseExactlyOnce() {
        RefreshTokenCleanupScheduler scheduler = new RefreshTokenCleanupScheduler(refreshTokenCleanupUseCase);
        when(refreshTokenCleanupUseCase.cleanupExpiredAndStaleRevokedTokens()).thenReturn(2);

        scheduler.cleanup();

        verify(refreshTokenCleanupUseCase).cleanupExpiredAndStaleRevokedTokens();
    }
}
