package com.tripgoapi.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record TourDetailResponse(
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
        List<TourImageResponse> images,
        List<String> highlights,
        List<TourItineraryDayResponse> itinerary,
        List<String> includes,
        List<String> excludes
) {
}
