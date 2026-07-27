package com.tripgoapi.domain.exception;

public class EmailAlreadyExistsException extends ConflictException {

    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
