package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;

import java.time.LocalDate;
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

    /**
     * Idempotency keys are client-generated and only unique per submitting user — never look
     * this up without also constraining by userId, or one user's retried request could return
     * another user's booking.
     */
    Optional<Booking> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    /**
     * Same lookup as {@link #findByUserIdAndIdempotencyKey}, but always runs in a brand new
     * transaction. Needed to recover after a failed {@link #save} caused by a concurrent request
     * winning the same (userId, idempotencyKey) race: Postgres aborts the whole transaction on a
     * unique-constraint violation, so no further statement can run in the caller's transaction
     * until it rolls back.
     */
    Optional<Booking> findByUserIdAndIdempotencyKeyInNewTransaction(Long userId, String idempotencyKey);

    Optional<Booking> findById(Long id);

    /**
     * Atomically transitions PENDING/CONFIRMED -> CANCELLED in a single statement, so two
     * concurrent cancel requests for the same booking can't both succeed and double-release slots.
     * @return true if the transition happened (booking existed and was in a cancellable state)
     */
    boolean cancelIfCancellable(Long id);

    /**
     * Gates review creation to users who both booked the tour and have actually taken the trip: a
     * booking in one of {@code statuses} whose departure date is on or before
     * {@code onOrBeforeDate}. A CONFIRMED booking whose departure hasn't happened yet must not
     * qualify — reviews cannot be edited (UNIQUE(tour_id, user_id), no update endpoint), so
     * letting it qualify would let a user "use up" their one review before ever taking the tour.
     *
     * @param onOrBeforeDate typically "today"; passed in by the caller rather than resolved here
     *                       so the "has the trip happened" rule stays visible and testable in the
     *                       service that owns it
     */
    boolean existsReviewEligibleBooking(Long userId, Long tourId, List<BookingStatus> statuses, LocalDate onOrBeforeDate);
}
