package com.tripgoapi.domain.exception;

public abstract class ForbiddenException extends RuntimeException {

    protected ForbiddenException(String message) {
        super(message);
    }
}
