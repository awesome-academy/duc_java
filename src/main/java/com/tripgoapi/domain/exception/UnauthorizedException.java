package com.tripgoapi.domain.exception;

public abstract class UnauthorizedException extends RuntimeException {

    protected UnauthorizedException(String message) {
        super(message);
    }
}
