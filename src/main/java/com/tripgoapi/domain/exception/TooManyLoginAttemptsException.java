package com.tripgoapi.domain.exception;

public class TooManyLoginAttemptsException extends TooManyRequestsException {

    public TooManyLoginAttemptsException() {
        super("Quá nhiều lần đăng nhập sai, vui lòng thử lại sau");
    }
}
