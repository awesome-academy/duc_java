package com.tripgoapi.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record BookingResponse(
        Long id,
        String bookingCode,
        Long tourId,
        LocalDate departureDate,
        int adults,
        int children,
        BigDecimal totalPrice,
        String status,
        String contactName,
        String contactEmail,
        String contactPhone,
        OffsetDateTime createdAt
) {
}
