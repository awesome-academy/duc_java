package com.tripgoapi.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull(message = "rating không được để trống")
        @Min(value = 1, message = "rating phải >= 1")
        @Max(value = 5, message = "rating phải <= 5")
        Integer rating,

        @Size(max = 2000, message = "comment tối đa 2000 ký tự")
        String comment
) {
}
