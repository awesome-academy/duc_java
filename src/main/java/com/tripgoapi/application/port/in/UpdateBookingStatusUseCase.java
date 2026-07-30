package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;

public interface UpdateBookingStatusUseCase {

    /**
     * Admin-driven status transition. Only PENDING -> CONFIRMED and PENDING/CONFIRMED -> CANCELLED
     * are allowed; cancelling also releases the reserved departure slots.
     *
     * @throws com.tripgoapi.domain.exception.BookingNotFoundException          if no such booking
     * @throws com.tripgoapi.domain.exception.InvalidBookingStatusException     if the transition
     *                                                                         is not permitted
     *                                                                         from the booking's
     *                                                                         current status
     */
    Booking updateStatus(Long bookingId, BookingStatus targetStatus);
}
