package com.tripgoapi.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record TourCardResponse(
        Long id,
        String title,
        String slug,
        Long destinationId,
        String destinationName,
        BigDecimal price,
        BigDecimal discountPrice,
        Integer durationDays,
        BigDecimal ratingAvg,
        Integer reviewCount,
        boolean featured
) {
}
