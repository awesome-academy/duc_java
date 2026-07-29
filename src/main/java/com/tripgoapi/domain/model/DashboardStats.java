package com.tripgoapi.domain.model;

import java.math.BigDecimal;

/** Aggregate counters shown on the admin dashboard tiles. */
public record DashboardStats(
        long activeTours,
        long destinations,
        long pendingBookings,
        long confirmedBookings,
        long customers,
        BigDecimal revenue
) {
}
