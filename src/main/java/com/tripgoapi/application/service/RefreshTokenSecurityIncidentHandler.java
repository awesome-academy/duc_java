package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.RefreshTokenRepositoryInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Chạy trong transaction riêng (REQUIRES_NEW) để việc thu hồi không bị cuốn theo
 * rollback khi RefreshTokenService ném InvalidRefreshTokenException ngay sau đó.
 */
@Component
@RequiredArgsConstructor
class RefreshTokenSecurityIncidentHandler {

    private final RefreshTokenRepositoryInterface refreshTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void revokeAllSessions(Long userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }
}
