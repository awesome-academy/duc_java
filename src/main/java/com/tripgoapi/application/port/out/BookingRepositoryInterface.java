package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;

import java.util.List;
import java.util.Optional;

public interface BookingRepositoryInterface {

    /**
     * Persists a new booking. The given {@code booking} has no {@code id}/{@code bookingCode}
     * yet; the returned instance has both populated.
     */
    Booking save(Booking booking);

    /**
     * @param status optional filter; {@code null} returns bookings in any status
     */
    List<Booking> findByUserId(Long userId, BookingStatus status);

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    Optional<Booking> findById(Long id);

    /**
     * Atomically transitions PENDING/CONFIRMED -> CANCELLED in a single statement, so two
     * concurrent cancel requests for the same booking can't both succeed and double-release slots.
     * @return true if the transition happened (booking existed and was in a cancellable state)
     */
    boolean cancelIfCancellable(Long id);
}
