package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Booking;

public interface GetBookingDetailUseCase {

    /**
     * @param requesterId id of the currently authenticated user; enforces that only the
     *                     booking's owner may view it
     */
    Booking getBookingDetail(Long bookingId, Long requesterId);
}
