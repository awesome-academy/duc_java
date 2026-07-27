package com.tripgoapi.application.port.out;

public record AccessToken(String value, String tokenType, long expiresInSeconds) {
}
