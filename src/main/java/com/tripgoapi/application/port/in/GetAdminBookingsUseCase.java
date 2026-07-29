package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Booking;

public interface GetAdminBookingsUseCase {

    /**
     * Every booking in the system, newest first — unlike {@link GetBookingsUseCase}, which is
     * scoped to the calling customer.
     */
    PageResult<Booking> searchBookings(AdminBookingSearchQuery query);
}
