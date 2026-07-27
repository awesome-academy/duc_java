package com.tripgoapi.infrastructure.adapter.in.scheduler;

import com.tripgoapi.application.port.in.RefreshTokenCleanupUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupScheduler.class);

    private final RefreshTokenCleanupUseCase refreshTokenCleanupUseCase;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanup() {
        int deleted = refreshTokenCleanupUseCase.cleanupExpiredAndStaleRevokedTokens();
        log.info("Refresh token cleanup removed {} row(s)", deleted);
    }
}
