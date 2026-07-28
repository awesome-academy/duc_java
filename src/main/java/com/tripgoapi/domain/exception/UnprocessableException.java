package com.tripgoapi.domain.exception;

public abstract class UnprocessableException extends RuntimeException {

    protected UnprocessableException(String message) {
        super(message);
    }
}
