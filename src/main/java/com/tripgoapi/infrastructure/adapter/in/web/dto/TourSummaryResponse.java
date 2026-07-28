package com.tripgoapi.infrastructure.adapter.in.web.dto;

public record TourSummaryResponse(
        Long id,
        String title,
        String slug,
        Integer durationDays
) {
}
