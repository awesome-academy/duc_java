package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBookingsServiceTest {

    @Mock
    private BookingRepositoryInterface bookingRepository;

    @Test
    void getBookingsForUser_delegatesToRepository() {
        GetBookingsService service = new GetBookingsService(bookingRepository);
        Booking booking = new Booking(
                1L, "TG-2026-000001", "idem-key-1", 5L, 2L, "Da Nang Tour", "da-nang-tour", 3,
                3L, LocalDate.of(2026, 8, 15),
                2, 0, BigDecimal.valueOf(200), BookingStatus.PENDING,
                "Jane", "jane@example.com", "0900000000", OffsetDateTime.now()
        );
        when(bookingRepository.findByUserId(5L, null)).thenReturn(List.of(booking));

        List<Booking> result = service.getBookingsForUser(5L, null);

        assertThat(result).containsExactly(booking);
    }

    @Test
    void getBookingsForUser_passesStatusFilterThrough() {
        GetBookingsService service = new GetBookingsService(bookingRepository);
        when(bookingRepository.findByUserId(5L, BookingStatus.CANCELLED)).thenReturn(List.of());

        service.getBookingsForUser(5L, BookingStatus.CANCELLED);

        org.mockito.Mockito.verify(bookingRepository).findByUserId(5L, BookingStatus.CANCELLED);
    }
}
