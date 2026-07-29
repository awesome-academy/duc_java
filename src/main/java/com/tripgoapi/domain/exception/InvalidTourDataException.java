package com.tripgoapi.domain.exception;

public class InvalidTourDataException extends UnprocessableException {

    public InvalidTourDataException(String message) {
        super(message);
    }
}
