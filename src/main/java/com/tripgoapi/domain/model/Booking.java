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
            Long tourId,
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
                null, null, idempotencyKey, userId, tourId, departureId, departureDate,
                adults, children, totalPrice, BookingStatus.PENDING,
                contactName, contactEmail, contactPhone, null
        );
    }
}
