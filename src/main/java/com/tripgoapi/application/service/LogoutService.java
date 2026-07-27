package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.LogoutUseCase;
import com.tripgoapi.application.port.out.RefreshTokenGeneratorPort;
import com.tripgoapi.application.port.out.RefreshTokenRepositoryInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepositoryInterface refreshTokenRepository;
    private final RefreshTokenGeneratorPort refreshTokenGenerator;

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = refreshTokenGenerator.hash(rawRefreshToken);
        refreshTokenRepository.revokeIfActive(hash);
    }
}
