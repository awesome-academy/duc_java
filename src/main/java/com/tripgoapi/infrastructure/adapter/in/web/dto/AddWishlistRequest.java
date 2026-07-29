package com.tripgoapi.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddWishlistRequest(
        @NotNull(message = "tourId không được để trống")
        @Positive(message = "tourId phải > 0")
        Long tourId
) {
}
