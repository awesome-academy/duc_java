package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.domain.exception.BookingAccessDeniedException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBookingDetailServiceTest {

    private static final Long BOOKING_ID = 1L;
    private static final Long OWNER_ID = 5L;

    @Mock
    private BookingRepositoryInterface bookingRepository;

    private GetBookingDetailService service;

    private Booking bookingOwnedBy(Long userId) {
        return new Booking(
                BOOKING_ID, "TG-2026-000001", "idem-key-1", userId, 2L, "Da Nang Tour", "da-nang-tour", 3,
                3L, LocalDate.of(2026, 8, 15),
                2, 0, BigDecimal.valueOf(200), BookingStatus.PENDING,
                "Jane", "jane@example.com", "0900000000", OffsetDateTime.now()
        );
    }

    @Test
    void bookingOwnedByRequester_returnsBooking() {
        service = new GetBookingDetailService(bookingRepository);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(bookingOwnedBy(OWNER_ID)));

        Booking result = service.getBookingDetail(BOOKING_ID, OWNER_ID);

        assertThat(result.id()).isEqualTo(BOOKING_ID);
    }

    @Test
    void bookingNotFound_throwsBookingNotFoundException() {
        service = new GetBookingDetailService(bookingRepository);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBookingDetail(BOOKING_ID, OWNER_ID))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void bookingOwnedByAnotherUser_throwsBookingAccessDeniedException() {
        service = new GetBookingDetailService(bookingRepository);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(bookingOwnedBy(OWNER_ID)));

        assertThatThrownBy(() -> service.getBookingDetail(BOOKING_ID, 999L))
                .isInstanceOf(BookingAccessDeniedException.class);
    }
}
