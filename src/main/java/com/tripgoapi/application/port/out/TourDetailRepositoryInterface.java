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
     */
    void recalculateRatingStats(Long tourId);
}
