package com.tripgoapi.domain.model;

import java.math.BigDecimal;

public record Tour(
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
