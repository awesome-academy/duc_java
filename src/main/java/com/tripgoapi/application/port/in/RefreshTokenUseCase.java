package com.tripgoapi.application.port.in;

public interface RefreshTokenUseCase {
    AuthToken refresh(String refreshToken);
}
