package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepositoryInterface {

    /**
     * Persists a new booking. The given {@code booking} has no {@code id}/{@code bookingCode}
     * yet; the returned instance has both populated.
     */
    Booking save(Booking booking);

    List<Booking> findByUserId(Long userId);

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
}
