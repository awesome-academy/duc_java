package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.BookingEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourDepartureEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.BookingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingPersistenceAdapter implements BookingRepositoryInterface {

    private final BookingJpaRepository bookingJpaRepository;

    @Override
    public Booking save(Booking booking) {
        BookingEntity entity = BookingEntity.builder()
                // Temporary placeholder: booking_code is NOT NULL UNIQUE, and the real code is
                // derived from the id Hibernate only assigns once this row is actually inserted.
                // Never visible outside this transaction.
                .bookingCode(UUID.randomUUID().toString())
                .idempotencyKey(booking.idempotencyKey())
                .user(UserEntity.builder().id(booking.userId()).build())
                .tour(TourEntity.builder().id(booking.tourId()).build())
                .departure(TourDepartureEntity.builder()
                        .id(booking.departureId())
                        .departureDate(booking.departureDate())
                        .build())
                .adults(booking.adults())
                .children(booking.children())
                .totalPrice(booking.totalPrice())
                .status(booking.status().name())
                .contactName(booking.contactName())
                .contactEmail(booking.contactEmail())
                .contactPhone(booking.contactPhone())
                .createdAt(OffsetDateTime.now())
                .build();

        // IDENTITY generation inserts eagerly, so the id is populated right after this call.
        BookingEntity inserted = bookingJpaRepository.save(entity);
        inserted.setBookingCode(generateBookingCode(inserted.getId(), inserted.getCreatedAt()));
        BookingEntity saved = bookingJpaRepository.save(inserted);

        return toDomain(saved);
    }

    @Override
    public List<Booking> findByUserId(Long userId) {
        return bookingJpaRepository.findByUserIdWithDeparture(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Booking> findByIdempotencyKey(String idempotencyKey) {
        return bookingJpaRepository.findByIdempotencyKeyWithDeparture(idempotencyKey).map(this::toDomain);
    }

    private String generateBookingCode(Long id, OffsetDateTime createdAt) {
        return "TG-%d-%06d".formatted(createdAt.getYear(), id);
    }

    private Booking toDomain(BookingEntity entity) {
        return new Booking(
                entity.getId(),
                entity.getBookingCode(),
                entity.getIdempotencyKey(),
                entity.getUser().getId(),
                entity.getTour().getId(),
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
