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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

        // Built from the input `booking`, not via toDomain(saved): saved.getTour()/.getUser()/
        // .getDeparture() are stub entities holding only the FK id set above, so reading any
        // other field off them (e.g. tour title) would silently come back null.
        return new Booking(
                saved.getId(), saved.getBookingCode(), booking.idempotencyKey(), booking.userId(), booking.tourId(),
                booking.tourTitle(), booking.tourSlug(), booking.tourDurationDays(),
                booking.departureId(), booking.departureDate(), booking.adults(), booking.children(),
                booking.totalPrice(), booking.status(), booking.contactName(), booking.contactEmail(),
                booking.contactPhone(), saved.getCreatedAt()
        );
    }

    @Override
    public List<Booking> findByUserId(Long userId, BookingStatus status) {
        return bookingJpaRepository.findByUserIdWithDeparture(userId, status == null ? null : status.name())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Booking> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey) {
        return bookingJpaRepository.findByUserIdAndIdempotencyKeyWithDeparture(userId, idempotencyKey).map(this::toDomain);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<Booking> findByUserIdAndIdempotencyKeyInNewTransaction(Long userId, String idempotencyKey) {
        return findByUserIdAndIdempotencyKey(userId, idempotencyKey);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return bookingJpaRepository.findByIdWithDeparture(id).map(this::toDomain);
    }

    @Override
    public boolean cancelIfCancellable(Long id) {
        return bookingJpaRepository.cancelIfCancellable(id) > 0;
    }

    @Override
    public boolean existsReviewEligibleBooking(Long userId, Long tourId, List<BookingStatus> statuses, LocalDate onOrBeforeDate) {
        List<String> statusNames = statuses.stream().map(Enum::name).toList();
        return bookingJpaRepository.existsByUser_IdAndTour_IdAndStatusInAndDeparture_DepartureDateLessThanEqual(
                userId, tourId, statusNames, onOrBeforeDate);
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
