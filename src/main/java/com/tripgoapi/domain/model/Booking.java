package com.tripgoapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record Booking(
        Long id,
        String bookingCode,
        String idempotencyKey,
        Long userId,
        Long tourId,
        // Denormalized from the tour row, same pattern as departureDate below.
        String tourTitle,
        String tourSlug,
        Integer tourDurationDays,
        Long departureId,
        LocalDate departureDate,
        int adults,
        int children,
        BigDecimal totalPrice,
        BookingStatus status,
        String contactName,
        String contactEmail,
        String contactPhone,
        OffsetDateTime createdAt
) {

    public static Booking pending(
            String idempotencyKey,
            Long userId,
            TourDetail tour,
            Long departureId,
            LocalDate departureDate,
            int adults,
            int children,
            BigDecimal totalPrice,
            String contactName,
            String contactEmail,
            String contactPhone
    ) {
        return new Booking(
                null, null, idempotencyKey, userId, tour.id(), tour.title(), tour.slug(), tour.durationDays(),
                departureId, departureDate, adults, children, totalPrice, BookingStatus.PENDING,
                contactName, contactEmail, contactPhone, null
        );
    }
}
