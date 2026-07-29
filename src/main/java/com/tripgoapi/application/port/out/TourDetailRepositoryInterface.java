package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.TourDetail;

import java.math.BigDecimal;
import java.util.Optional;

public interface TourDetailRepositoryInterface {

    Optional<TourDetail> findById(Long id);

    boolean existsActiveTour(Long id);

    Optional<BigDecimal> findRatingAvg(Long id);

    /**
     * Recomputes rating_avg/review_count for this tour directly from the reviews table, so the
     * two derived columns can never drift from the actual review rows — called right after a
     * review insert instead of incrementing counters in application code.
     *
     * <p>On its own this is only immune to lost-update drift, not to concurrent-insert drift —
     * see {@link #lockActiveTourForReview} for the guard that closes that gap.
     */
    void recalculateRatingStats(Long tourId);

    /**
     * Locks the tour row for the duration of the caller's transaction and reports whether it
     * exists and is ACTIVE. Must be called (and awaited) before inserting a review for this tour:
     * it serializes two concurrent review submissions for the same tour, so the second
     * transaction's later {@link #recalculateRatingStats} call is guaranteed to see the first
     * transaction's already-committed review.
     *
     * @return true if the tour exists and is ACTIVE
     */
    boolean lockActiveTourForReview(Long id);
}
