package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Booking;

public interface CancelBookingUseCase {

    /**
     * @param requesterId id of the currently authenticated user; enforces that only the
     *                     booking's owner may cancel it
     */
    Booking cancelBooking(Long bookingId, Long requesterId);
}
