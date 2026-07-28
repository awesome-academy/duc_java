package com.tripgoapi.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// userId is deliberately not a field here — it must come from the authenticated JWT principal,
// never from client input.
public record CreateBookingRequest(
        // Client-generated, kept identical across retries of the same create intent (e.g. after
        // a timeout) so a resubmitted request returns the original booking instead of creating
        // a duplicate and double-reserving slots.
        @NotBlank(message = "idempotencyKey không được để trống")
        @Size(max = 100, message = "idempotencyKey tối đa 100 ký tự")
        String idempotencyKey,

        @NotNull(message = "tourId không được để trống")
        @Positive(message = "tourId phải > 0")
        Long tourId,

        @NotNull(message = "date không được để trống")
        @FutureOrPresent(message = "date phải là hôm nay hoặc trong tương lai")
        LocalDate date,

        @Min(value = 1, message = "adults phải >= 1")
        int adults,

        @PositiveOrZero(message = "children phải >= 0")
        int children,

        @NotNull(message = "contact không được để trống")
        @Valid
        ContactInfo contact
) {
}
