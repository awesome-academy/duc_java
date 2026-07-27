package com.tripgoapi.domain.exception;

public class NoAvailableSlotsException extends ConflictException {

    public NoAvailableSlotsException() {
        super("Ngày khởi hành đã hết chỗ");
    }
}
