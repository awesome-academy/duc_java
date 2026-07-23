package com.tripgoapi.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record TourDetail(
        Long id,
        String title,
        String slug,
        String description,
        Long destinationId,
        String destinationName,
        Integer durationDays,
        Integer maxGuests,
        BigDecimal price,
        BigDecimal discountPrice,
        BigDecimal ratingAvg,
        Integer reviewCount,
        List<TourImage> images,
        List<String> highlights,
        List<TourItineraryDay> itinerary,
        List<String> includes,
        List<String> excludes
) {
}
