package com.tripgoapi.domain.exception;

public class InvalidBookingStatusException extends UnprocessableException {

    public InvalidBookingStatusException(String status) {
        super("status không hợp lệ: " + status);
    }
}
