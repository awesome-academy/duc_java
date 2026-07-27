package com.tripgoapi.domain.exception;

public class BookingGroupTooLargeException extends ConflictException {

    public BookingGroupTooLargeException(int guestCount, int maxGuests) {
        super("Số khách (" + guestCount + ") vượt quá giới hạn " + maxGuests + " khách/đơn của tour");
    }
}
