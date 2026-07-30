package com.tripgoapi.application.port.in;

/**
 * Admin tour list query. Unlike {@link TourSearchQuery} this deliberately has no status filter:
 * the admin list always shows ACTIVE + INACTIVE tours and always hides DELETED ones.
 */
public record AdminTourSearchQuery(String keyword, int page, int size) {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    public AdminTourSearchQuery {
        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = DEFAULT_SIZE;
        } else if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }
}
