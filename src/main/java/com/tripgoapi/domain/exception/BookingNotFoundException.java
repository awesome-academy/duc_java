package com.tripgoapi.domain.exception;

public class BookingNotFoundException extends NotFoundException {

    public BookingNotFoundException(Long id) {
        super("Booking not found: id=" + id);
    }
}
