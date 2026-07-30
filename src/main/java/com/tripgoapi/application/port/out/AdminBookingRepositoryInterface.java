package com.tripgoapi.application.port.out;

import com.tripgoapi.application.port.in.AdminBookingSearchQuery;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.domain.model.Booking;

/**
 * Cross-customer booking access, only ever reachable from the admin portal.
 * {@link BookingRepositoryInterface} stays scoped to a single user's bookings.
 */
public interface AdminBookingRepositoryInterface {

    PageResult<Booking> searchBookings(AdminBookingSearchQuery query);

    /**
     * Atomically transitions PENDING -> CONFIRMED in a single statement, so two admins confirming
     * the same booking cannot both believe they made the transition.
     *
     * @return true if the transition happened (booking existed and was still PENDING)
     */
    boolean confirmIfPending(Long id);
}
