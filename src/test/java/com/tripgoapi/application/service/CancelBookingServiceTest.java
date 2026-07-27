package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.application.port.out.TourDepartureRepositoryInterface;
import com.tripgoapi.domain.exception.BookingAccessDeniedException;
import com.tripgoapi.domain.exception.BookingCancellationNotAllowedException;
import com.tripgoapi.domain.exception.BookingNotFoundException;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelBookingServiceTest {

    private static final Long BOOKING_ID = 1L;
    private static final Long OWNER_ID = 5L;
    private static final Long DEPARTURE_ID = 3L;

    @Mock
    private BookingRepositoryInterface bookingRepository;
    @Mock
    private TourDepartureRepositoryInterface tourDepartureRepository;

    private CancelBookingService service;

    private Booking booking(Long userId, BookingStatus status) {
        return new Booking(
                BOOKING_ID, "TG-2026-000001", "idem-key-1", userId, 2L, "Da Nang Tour", "da-nang-tour", 3,
                DEPARTURE_ID, LocalDate.of(2026, 8, 15),
                2, 1, BigDecimal.valueOf(300), status,
                "Jane", "jane@example.com", "0900000000", OffsetDateTime.now()
        );
    }

    @Test
    void bookingNotFound_throwsBookingNotFoundException_neverReleasesSlotsOrCancels() {
        service = new CancelBookingService(bookingRepository, tourDepartureRepository);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelBooking(BOOKING_ID, OWNER_ID))
                .isInstanceOf(BookingNotFoundException.class);

        verify(tourDepartureRepository, never()).releaseSlots(any(), anyInt());
        verify(bookingRepository, never()).cancelIfCancellable(any());
    }

    @Test
    void bookingOwnedByAnotherUser_throwsBookingAccessDeniedException_neverReleasesSlotsOrCancels() {
        service = new CancelBookingService(bookingRepository, tourDepartureRepository);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking(OWNER_ID, BookingStatus.PENDING)));

        assertThatThrownBy(() -> service.cancelBooking(BOOKING_ID, 999L))
                .isInstanceOf(BookingAccessDeniedException.class);

        verify(tourDepartureRepository, never()).releaseSlots(any(), anyInt());
        verify(bookingRepository, never()).cancelIfCancellable(any());
    }

    @Test
    void atomicCancelRejected_throwsBookingCancellationNotAllowedException_neverReleasesSlots() {
        // Covers both "already cancelled/completed" and "lost a concurrent cancel race":
        // the atomic UPDATE ... WHERE status IN (PENDING, CONFIRMED) is the single source of
        // truth for whether the transition is allowed, not the in-memory status read earlier.
        service = new CancelBookingService(bookingRepository, tourDepartureRepository);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking(OWNER_ID, BookingStatus.CANCELLED)));
        when(bookingRepository.cancelIfCancellable(BOOKING_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.cancelBooking(BOOKING_ID, OWNER_ID))
                .isInstanceOf(BookingCancellationNotAllowedException.class);

        verify(tourDepartureRepository, never()).releaseSlots(any(), anyInt());
    }

    @Test
    void pendingBookingOwnedByRequester_releasesSlots_andReturnsCancelledBooking() {
        service = new CancelBookingService(bookingRepository, tourDepartureRepository);
        Booking pending = booking(OWNER_ID, BookingStatus.PENDING);
        Booking cancelled = booking(OWNER_ID, BookingStatus.CANCELLED);
        when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(pending))
                .thenReturn(Optional.of(cancelled));
        when(bookingRepository.cancelIfCancellable(BOOKING_ID)).thenReturn(true);

        Booking result = service.cancelBooking(BOOKING_ID, OWNER_ID);

        assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
        verify(tourDepartureRepository).releaseSlots(DEPARTURE_ID, 3);
        verify(bookingRepository).cancelIfCancellable(BOOKING_ID);
    }

    @Test
    void confirmedBookingOwnedByRequester_canAlsoBeCancelled() {
        service = new CancelBookingService(bookingRepository, tourDepartureRepository);
        when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(booking(OWNER_ID, BookingStatus.CONFIRMED)))
                .thenReturn(Optional.of(booking(OWNER_ID, BookingStatus.CANCELLED)));
        when(bookingRepository.cancelIfCancellable(BOOKING_ID)).thenReturn(true);

        Booking result = service.cancelBooking(BOOKING_ID, OWNER_ID);

        assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
    }
}
