package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.BookingEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourDepartureEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.BookingJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingPersistenceAdapterTest {

    @Mock
    private BookingJpaRepository bookingJpaRepository;

    private BookingPersistenceAdapter adapter;

    private BookingPersistenceAdapter newAdapter() {
        return new BookingPersistenceAdapter(bookingJpaRepository);
    }

    private Booking newBooking() {
        return new Booking(
                null, null, "idem-key-1", 5L, 2L, null, null, null, 3L, LocalDate.of(2026, 8, 15),
                2, 1, BigDecimal.valueOf(300), BookingStatus.PENDING,
                "Jane", "jane@example.com", "0900000000", null
        );
    }

    @Test
    void save_insertsThenAssignsFinalCodeDerivedFromGeneratedId() {
        adapter = newAdapter();
        // Mimics IDENTITY generation: first save() (id == null) assigns an id; the second
        // save() (the code-update) just returns the already-populated entity, like a real
        // managed-entity update would.
        when(bookingJpaRepository.save(org.mockito.ArgumentMatchers.any(BookingEntity.class))).thenAnswer(invocation -> {
            BookingEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(123L);
            }
            return entity;
        });

        Booking result = adapter.save(newBooking());

        assertThat(result.id()).isEqualTo(123L);
        assertThat(result.bookingCode()).isEqualTo("TG-%d-000123".formatted(OffsetDateTime.now().getYear()));
        verify(bookingJpaRepository, times(2)).save(org.mockito.ArgumentMatchers.any(BookingEntity.class));
    }

    @Test
    void save_persistsAllDomainFieldsIntoTheEntity() {
        adapter = newAdapter();
        when(bookingJpaRepository.save(org.mockito.ArgumentMatchers.any(BookingEntity.class))).thenAnswer(invocation -> {
            BookingEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(1L);
            }
            return entity;
        });

        Booking result = adapter.save(newBooking());

        assertThat(result.userId()).isEqualTo(5L);
        assertThat(result.tourId()).isEqualTo(2L);
        assertThat(result.departureId()).isEqualTo(3L);
        assertThat(result.departureDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(result.adults()).isEqualTo(2);
        assertThat(result.children()).isEqualTo(1);
        assertThat(result.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(result.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(result.contactName()).isEqualTo("Jane");
        assertThat(result.contactEmail()).isEqualTo("jane@example.com");
        assertThat(result.contactPhone()).isEqualTo("0900000000");
        assertThat(result.idempotencyKey()).isEqualTo("idem-key-1");
    }

    @Test
    void save_returnsTourSummaryFromInputBooking_notFromTheStubTourEntity() {
        // BookingPersistenceAdapter builds the tour association from just an id
        // (TourEntity.builder().id(x).build()) to set the FK — it never loads the real row, so
        // the returned Booking must carry the summary through from the caller, not re-derive it
        // from that stub entity (which would silently come back null for title/slug/duration).
        adapter = newAdapter();
        when(bookingJpaRepository.save(org.mockito.ArgumentMatchers.any(BookingEntity.class))).thenAnswer(invocation -> {
            BookingEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(1L);
            }
            return entity;
        });
        Booking withTourSummary = new Booking(
                null, null, "idem-key-1", 5L, 2L, "Da Nang Tour", "da-nang-tour", 3, 3L,
                LocalDate.of(2026, 8, 15), 2, 1, BigDecimal.valueOf(300), BookingStatus.PENDING,
                "Jane", "jane@example.com", "0900000000", null
        );

        Booking result = adapter.save(withTourSummary);

        assertThat(result.tourTitle()).isEqualTo("Da Nang Tour");
        assertThat(result.tourSlug()).isEqualTo("da-nang-tour");
        assertThat(result.tourDurationDays()).isEqualTo(3);
    }

    @Test
    void findByUserIdAndIdempotencyKey_found_mapsEntity() {
        adapter = newAdapter();
        BookingEntity entity = BookingEntity.builder()
                .id(1L)
                .bookingCode("TG-2026-000001")
                .idempotencyKey("idem-key-1")
                .user(UserEntity.builder().id(5L).build())
                .tour(TourEntity.builder().id(2L).build())
                .departure(TourDepartureEntity.builder().id(3L).departureDate(LocalDate.of(2026, 8, 15)).build())
                .adults(2).children(0)
                .totalPrice(BigDecimal.valueOf(200))
                .status("PENDING")
                .createdAt(OffsetDateTime.now())
                .build();
        when(bookingJpaRepository.findByUserIdAndIdempotencyKeyWithDeparture(5L, "idem-key-1"))
                .thenReturn(java.util.Optional.of(entity));

        assertThat(adapter.findByUserIdAndIdempotencyKey(5L, "idem-key-1"))
                .map(Booking::bookingCode).contains("TG-2026-000001");
    }

    @Test
    void findByUserIdAndIdempotencyKey_notFound_returnsEmpty() {
        adapter = newAdapter();
        when(bookingJpaRepository.findByUserIdAndIdempotencyKeyWithDeparture(5L, "unknown"))
                .thenReturn(java.util.Optional.empty());

        assertThat(adapter.findByUserIdAndIdempotencyKey(5L, "unknown")).isEmpty();
    }

    @Test
    void findByUserIdAndIdempotencyKeyInNewTransaction_delegatesToTheSameScopedLookup() {
        adapter = newAdapter();
        BookingEntity entity = BookingEntity.builder()
                .id(1L)
                .bookingCode("TG-2026-000001")
                .idempotencyKey("idem-key-1")
                .user(UserEntity.builder().id(5L).build())
                .tour(TourEntity.builder().id(2L).build())
                .departure(TourDepartureEntity.builder().id(3L).departureDate(LocalDate.of(2026, 8, 15)).build())
                .adults(2).children(0)
                .totalPrice(BigDecimal.valueOf(200))
                .status("PENDING")
                .createdAt(OffsetDateTime.now())
                .build();
        when(bookingJpaRepository.findByUserIdAndIdempotencyKeyWithDeparture(5L, "idem-key-1"))
                .thenReturn(java.util.Optional.of(entity));

        assertThat(adapter.findByUserIdAndIdempotencyKeyInNewTransaction(5L, "idem-key-1"))
                .map(Booking::bookingCode).contains("TG-2026-000001");
    }

    @Test
    void findByUserId_mapsEntitiesUsingTheJoinFetchQuery() {
        adapter = newAdapter();
        BookingEntity entity = BookingEntity.builder()
                .id(1L)
                .bookingCode("TG-2026-000001")
                .user(UserEntity.builder().id(5L).build())
                .tour(TourEntity.builder().id(2L).title("Da Nang Tour").slug("da-nang-tour").durationDays(3).build())
                .departure(TourDepartureEntity.builder().id(3L).departureDate(LocalDate.of(2026, 8, 15)).build())
                .adults(2).children(0)
                .totalPrice(BigDecimal.valueOf(200))
                .status("PENDING")
                .contactName("Jane").contactEmail("jane@example.com").contactPhone("0900000000")
                .createdAt(OffsetDateTime.now())
                .build();
        when(bookingJpaRepository.findByUserIdWithDeparture(5L, null)).thenReturn(List.of(entity));

        List<Booking> result = adapter.findByUserId(5L, null);

        assertThat(result).hasSize(1);
        Booking booking = result.get(0);
        assertThat(booking.bookingCode()).isEqualTo("TG-2026-000001");
        assertThat(booking.departureDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(booking.tourTitle()).isEqualTo("Da Nang Tour");
        assertThat(booking.tourSlug()).isEqualTo("da-nang-tour");
        assertThat(booking.tourDurationDays()).isEqualTo(3);
    }

    @Test
    void findByUserId_withStatusFilter_passesStatusNameToQuery() {
        adapter = newAdapter();
        when(bookingJpaRepository.findByUserIdWithDeparture(5L, "CANCELLED")).thenReturn(List.of());

        adapter.findByUserId(5L, BookingStatus.CANCELLED);

        verify(bookingJpaRepository).findByUserIdWithDeparture(5L, "CANCELLED");
    }

    @Test
    void findById_found_mapsEntity() {
        adapter = newAdapter();
        BookingEntity entity = BookingEntity.builder()
                .id(1L)
                .bookingCode("TG-2026-000001")
                .user(UserEntity.builder().id(5L).build())
                .tour(TourEntity.builder().id(2L).build())
                .departure(TourDepartureEntity.builder().id(3L).departureDate(LocalDate.of(2026, 8, 15)).build())
                .adults(2).children(0)
                .totalPrice(BigDecimal.valueOf(200))
                .status("PENDING")
                .createdAt(OffsetDateTime.now())
                .build();
        when(bookingJpaRepository.findByIdWithDeparture(1L)).thenReturn(java.util.Optional.of(entity));

        assertThat(adapter.findById(1L)).map(Booking::bookingCode).contains("TG-2026-000001");
    }

    @Test
    void findById_notFound_returnsEmpty() {
        adapter = newAdapter();
        when(bookingJpaRepository.findByIdWithDeparture(1L)).thenReturn(java.util.Optional.empty());

        assertThat(adapter.findById(1L)).isEmpty();
    }

    @Test
    void cancelIfCancellable_oneRowAffected_returnsTrue() {
        adapter = newAdapter();
        when(bookingJpaRepository.cancelIfCancellable(1L)).thenReturn(1);

        assertThat(adapter.cancelIfCancellable(1L)).isTrue();
    }

    @Test
    void cancelIfCancellable_zeroRowsAffected_returnsFalse() {
        // Zero rows means the WHERE status IN (PENDING, CONFIRMED) predicate didn't match —
        // either not found, already cancelled/completed, or lost a concurrent cancel race.
        adapter = newAdapter();
        when(bookingJpaRepository.cancelIfCancellable(1L)).thenReturn(0);

        assertThat(adapter.cancelIfCancellable(1L)).isFalse();
    }
}
