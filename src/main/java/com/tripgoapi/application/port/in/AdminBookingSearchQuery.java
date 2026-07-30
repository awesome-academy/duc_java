package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.BookingStatus;

/**
 * @param status optional filter; {@code null} means "Tất cả" (every status)
 */
public record AdminBookingSearchQuery(BookingStatus status, int page, int size) {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    public AdminBookingSearchQuery {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = DEFAULT_SIZE;
        } else if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }
}
