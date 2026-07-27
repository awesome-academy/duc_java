package com.tripgoapi.domain.exception;

public class InvalidRefreshTokenException extends UnauthorizedException {

    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token");
    }
}
