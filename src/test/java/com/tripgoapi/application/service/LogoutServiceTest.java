package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.RefreshTokenGeneratorPort;
import com.tripgoapi.application.port.out.RefreshTokenRepositoryInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private RefreshTokenRepositoryInterface refreshTokenRepository;
    @Mock
    private RefreshTokenGeneratorPort refreshTokenGenerator;

    @Test
    void hashesRawTokenAndRevokesIt_idempotentlyIgnoringResult() {
        LogoutService service = new LogoutService(refreshTokenRepository, refreshTokenGenerator);
        when(refreshTokenGenerator.hash("raw")).thenReturn("hash");

        service.logout("raw");

        verify(refreshTokenRepository).revokeIfActive("hash");
    }
}
