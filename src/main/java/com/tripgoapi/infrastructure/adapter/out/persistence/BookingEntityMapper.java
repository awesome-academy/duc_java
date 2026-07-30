package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.BookingEntity;

/**
 * Shared by the customer-scoped and admin-scoped booking adapters so the two cannot drift.
 * Callers must have fetched {@code tour} and {@code departure} — reading them off a lazy proxy
 * outside a session would fail.
 */
final class BookingEntityMapper {

    private BookingEntityMapper() {
    }

    static Booking toDomain(BookingEntity entity) {
        return new Booking(
                entity.getId(),
                entity.getBookingCode(),
                entity.getIdempotencyKey(),
                entity.getUser().getId(),
                entity.getTour().getId(),
                entity.getTour().getTitle(),
                entity.getTour().getSlug(),
                entity.getTour().getDurationDays(),
                entity.getDeparture().getId(),
                entity.getDeparture().getDepartureDate(),
                entity.getAdults(),
                entity.getChildren(),
                entity.getTotalPrice(),
                BookingStatus.valueOf(entity.getStatus()),
                entity.getContactName(),
                entity.getContactEmail(),
                entity.getContactPhone(),
                entity.getCreatedAt()
        );
    }
}
