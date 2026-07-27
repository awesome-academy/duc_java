package com.tripgoapi.application.port.out;

public interface RefreshTokenGeneratorPort {

    GeneratedRefreshToken generate();

    String hash(String rawToken);
}
