package com.tripgoapi.application.port.in;

public record AuthToken(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
}
