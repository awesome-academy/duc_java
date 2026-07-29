package com.tripgoapi.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Everything the admin edit form needs for one tour. Separate from {@link TourDetail} (the public
 * detail payload): that one is restricted to ACTIVE tours and omits the editable bookkeeping
 * fields — status, featured flag and category — the admin form has to round-trip.
 */
public record AdminTourDetail(
        Long id,
        String title,
        String slug,
        String description,
        Long destinationId,
        String destinationName,
        Long categoryId,
        String categoryName,
        BigDecimal price,
        BigDecimal discountPrice,
        Integer durationDays,
        Integer maxGuests,
        boolean featured,
        TourStatus status,
        List<TourImage> images,
        List<TourItineraryDay> itinerary
) {
}
