package com.tripgoapi.application.port.in;

import java.time.LocalDate;

public record CreateBookingCommand(
        String idempotencyKey,
        Long userId,
        Long tourId,
        LocalDate date,
        int adults,
        int children,
        String contactName,
        String contactEmail,
        String contactPhone
) {
}
