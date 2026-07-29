package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.AdminBookingRepositoryInterface;
import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.application.port.out.TourDepartureRepositoryInterface;
import com.tripgoapi.domain.exception.BookingNotFoundException;
import com.tripgoapi.domain.exception.BookingStatusTransitionNotAllowedException;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
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
class UpdateBookingStatusServiceTest {

    private static final Long BOOKING_ID = 11L;
    private static final Long DEPARTURE_ID = 4L;

    @Mock
    private BookingRepositoryInterface bookingRepository;
    @Mock
    private AdminBookingRepositoryInterface adminBookingRepository;
    @Mock
    private TourDepartureRepositoryInterface tourDepartureRepository;

    private UpdateBookingStatusService service;

    @BeforeEach
    void setUp() {
        service = new UpdateBookingStatusService(
                bookingRepository, adminBookingRepository, tourDepartureRepository);
    }

    private Booking booking(BookingStatus status) {
        return new Booking(
                BOOKING_ID, "TG-2026-000011", "idem-11", 5L, 2L, "Đà Nẵng 3N2Đ", "da-nang-3n2d", 3,
                DEPARTURE_ID, LocalDate.of(2026, 8, 15),
                2, 1, BigDecimal.valueOf(11_225_000), status,
                "Nguyễn An", "an@example.com", "0900000000", OffsetDateTime.now()
        );
    }

    @Test
    void confirmingAPendingBooking_movesItToConfirmed_withoutTouchingSlots() {
        // Slots were already reserved when the booking was created; confirming must not reserve
        // them a second time.
        when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(booking(BookingStatus.PENDING)))
                .thenReturn(Optional.of(booking(BookingStatus.CONFIRMED)));
        when(adminBookingRepository.confirmIfPending(BOOKING_ID)).thenReturn(true);

        Booking result = service.updateStatus(BOOKING_ID, BookingStatus.CONFIRMED);

        assertThat(result.status()).isEqualTo(BookingStatus.CONFIRMED);
        verify(tourDepartureRepository, never()).releaseSlots(any(), anyInt());
    }

    @Test
    void cancellingReleasesTheReservedSlots_forAdultsAndChildrenTogether() {
        when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(booking(BookingStatus.CONFIRMED)))
                .thenReturn(Optional.of(booking(BookingStatus.CANCELLED)));
        when(bookingRepository.cancelIfCancellable(BOOKING_ID)).thenReturn(true);

        Booking result = service.updateStatus(BOOKING_ID, BookingStatus.CANCELLED);

        assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
        verify(tourDepartureRepository).releaseSlots(DEPARTURE_ID, 3);
    }

    @Test
    void confirmingAnAlreadyCancelledBooking_isRejected_andReportsTheRealCurrentStatus() {
        // The atomic UPDATE is the source of truth: when it matches nothing, the pre-read snapshot
        // is stale and the service must re-read before reporting what went wrong.
        when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(booking(BookingStatus.PENDING)))
                .thenReturn(Optional.of(booking(BookingStatus.CANCELLED)));
        when(adminBookingRepository.confirmIfPending(BOOKING_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.updateStatus(BOOKING_ID, BookingStatus.CONFIRMED))
                .isInstanceOf(BookingStatusTransitionNotAllowedException.class)
                .hasMessageContaining("CANCELLED");
    }

    @Test
    void cancellingAnUncancellableBooking_neverReleasesSlots() {
        // Releasing slots for a booking that was already cancelled would hand the departure back
        // capacity it never lost.
        when(bookingRepository.findById(BOOKING_ID))
                .thenReturn(Optional.of(booking(BookingStatus.COMPLETED)))
                .thenReturn(Optional.of(booking(BookingStatus.COMPLETED)));
        when(bookingRepository.cancelIfCancellable(BOOKING_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.updateStatus(BOOKING_ID, BookingStatus.CANCELLED))
                .isInstanceOf(BookingStatusTransitionNotAllowedException.class);

        verify(tourDepartureRepository, never()).releaseSlots(any(), anyInt());
    }

    @Test
    void movingABookingBackToPending_isRejectedOutright() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking(BookingStatus.CONFIRMED)));

        assertThatThrownBy(() -> service.updateStatus(BOOKING_ID, BookingStatus.PENDING))
                .isInstanceOf(BookingStatusTransitionNotAllowedException.class);

        verify(adminBookingRepository, never()).confirmIfPending(any());
        verify(bookingRepository, never()).cancelIfCancellable(any());
    }

    @Test
    void unknownBookingId_throwsBookingNotFound_beforeAnyTransitionIsAttempted() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(BOOKING_ID, BookingStatus.CONFIRMED))
                .isInstanceOf(BookingNotFoundException.class);

        verify(adminBookingRepository, never()).confirmIfPending(any());
    }
}
