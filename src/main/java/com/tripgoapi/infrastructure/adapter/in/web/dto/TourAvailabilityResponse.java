package com.tripgoapi.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record TourAvailabilityResponse(
        LocalDate departureDate,
        int totalSlots,
        int bookedSlots,
        @Schema(description = "Số chỗ còn = totalSlots - bookedSlots") int remainingSlots
) {
}
