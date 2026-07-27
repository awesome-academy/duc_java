package com.tripgoapi.application.port.in;

import java.math.BigDecimal;

public record TourSearchQuery(
        String keyword,
        String destinationSlug,
        String categorySlug,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer duration,
        BigDecimal minRating,
        Boolean featured,
        TourSortOption sort,
        int page,
        int size
) {
    private static final int DEFAULT_SIZE = 12;
    private static final int MAX_SIZE = 50;

    public TourSearchQuery {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = DEFAULT_SIZE;
        } else if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
        if (sort == null) {
            sort = TourSortOption.NEWEST;
        }
    }
}
