package com.tripgoapi.application.port.in;

public interface LogoutUseCase {
    void logout(String refreshToken);
}
