package com.tripgoapi.domain.model;

import java.math.BigDecimal;

/**
 * One row of the admin tour table. Distinct from {@link Tour} (the public tour card) because the
 * admin list also shows the tour's status and must include INACTIVE tours, which never reach
 * the public API.
 */
public record AdminTourSummary(
        Long id,
        String title,
        String destinationName,
        String categoryName,
        BigDecimal price,
        BigDecimal discountPrice,
        Integer durationDays,
        BigDecimal ratingAvg,
        TourStatus status
) {

    /** Price actually charged: the discount when one is set, otherwise the list price. */
    public BigDecimal effectivePrice() {
        return discountPrice != null ? discountPrice : price;
    }
}
