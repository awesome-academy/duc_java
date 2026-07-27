package com.tripgoapi.infrastructure.adapter.in.web.dto;

public record AuthTokenResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
}
