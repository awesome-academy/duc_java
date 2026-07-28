package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;

import java.util.List;

public interface GetBookingsUseCase {

    /**
     * @param status optional filter; {@code null} returns bookings in any status
     */
    List<Booking> getBookingsForUser(Long userId, BookingStatus status);
}
