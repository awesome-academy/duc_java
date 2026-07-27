package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.AuthToken;
import com.tripgoapi.application.port.out.AccessToken;
import com.tripgoapi.application.port.out.GeneratedRefreshToken;
import com.tripgoapi.application.port.out.RefreshTokenGeneratorPort;
import com.tripgoapi.application.port.out.RefreshTokenRepositoryInterface;
import com.tripgoapi.application.port.out.TokenProviderInterface;
import com.tripgoapi.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AuthTokenIssuer {

    private final TokenProviderInterface tokenProvider;
    private final RefreshTokenGeneratorPort refreshTokenGenerator;
    private final RefreshTokenRepositoryInterface refreshTokenRepository;

    AuthToken issue(Long userId, String email, Role role) {
        AccessToken accessToken = tokenProvider.generateToken(userId, email, role);
        GeneratedRefreshToken refreshToken = refreshTokenGenerator.generate();
        refreshTokenRepository.save(userId, refreshToken.hash(), refreshToken.expiresAt());

        return new AuthToken(accessToken.value(), refreshToken.rawValue(), accessToken.tokenType(), accessToken.expiresInSeconds());
    }
}
