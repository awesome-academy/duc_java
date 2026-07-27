package com.tripgoapi.domain.exception;

public abstract class TooManyRequestsException extends RuntimeException {

    protected TooManyRequestsException(String message) {
        super(message);
    }
}
