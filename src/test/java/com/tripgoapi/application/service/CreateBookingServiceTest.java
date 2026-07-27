package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.CreateBookingCommand;
import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.application.port.out.TourDepartureRepositoryInterface;
import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.domain.exception.BookingGroupTooLargeException;
import com.tripgoapi.domain.exception.NoAvailableSlotsException;
import com.tripgoapi.domain.exception.TourDepartureNotFoundException;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import com.tripgoapi.domain.model.TourDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateBookingServiceTest {

    private static final Long TOUR_ID = 1L;
    private static final Long DEPARTURE_ID = 42L;
    private static final LocalDate DATE = LocalDate.of(2026, 8, 15);
    private static final String IDEMPOTENCY_KEY = "idem-key-1";

    @Mock
    private TourDetailRepositoryInterface tourDetailRepository;
    @Mock
    private TourDepartureRepositoryInterface tourDepartureRepository;
    @Mock
    private BookingRepositoryInterface bookingRepository;

    private CreateBookingService service;

    @BeforeEach
    void setUp() {
        // Every call goes through the idempotency check first; default to "not seen before"
        // unless a specific test overrides it.
        when(bookingRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
    }

    private CreateBookingService newService() {
        return new CreateBookingService(tourDetailRepository, tourDepartureRepository, bookingRepository);
    }

    private TourDetail tourDetail(BigDecimal price, BigDecimal discountPrice) {
        return tourDetail(price, discountPrice, 20);
    }

    private TourDetail tourDetail(BigDecimal price, BigDecimal discountPrice, Integer maxGuests) {
        return new TourDetail(
                TOUR_ID, "Da Nang Tour", "da-nang-tour", "desc", 1L, "Da Nang",
                3, maxGuests, price, discountPrice, BigDecimal.valueOf(4.5), 10,
                List.of(), List.of(), List.of(), List.of(), List.of()
        );
    }

    private CreateBookingCommand command(Long userId, int adults, int children) {
        return new CreateBookingCommand(IDEMPOTENCY_KEY, userId, TOUR_ID, DATE, adults, children,
                "Jane", "jane@example.com", "0900000000");
    }

    @Test
    void idempotencyKeyAlreadyUsed_returnsExistingBooking_neverTouchesTourOrDepartureOrSaves() {
        service = newService();
        Booking existing = new Booking(
                9L, "TG-2026-000009", IDEMPOTENCY_KEY, 1L, TOUR_ID, DEPARTURE_ID, DATE,
                2, 0, BigDecimal.valueOf(200), BookingStatus.PENDING,
                "Jane", "jane@example.com", "0900000000", OffsetDateTime.now()
        );
        when(bookingRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.of(existing));

        Booking result = service.createBooking(command(1L, 2, 0));

        assertThat(result).isSameAs(existing);
        verifyNoInteractions(tourDetailRepository, tourDepartureRepository);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void tourNotFound_throwsTourNotFoundException_neverTouchesDepartureOrBooking() {
        service = newService();
        when(tourDetailRepository.findById(TOUR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createBooking(command(1L, 2, 0)))
                .isInstanceOf(TourNotFoundException.class);

        verifyNoInteractions(tourDepartureRepository);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void departureNotFound_throwsTourDepartureNotFoundException_neverReservesOrSaves() {
        service = newService();
        when(tourDetailRepository.findById(TOUR_ID)).thenReturn(Optional.of(tourDetail(BigDecimal.valueOf(100), null)));
        when(tourDepartureRepository.findDepartureId(TOUR_ID, DATE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createBooking(command(1L, 2, 0)))
                .isInstanceOf(TourDepartureNotFoundException.class);

        verify(tourDepartureRepository, never()).reserveSlots(any(), anyInt());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void groupLargerThanMaxGuests_throwsBookingGroupTooLargeException_neverReservesOrSaves() {
        service = newService();
        when(tourDetailRepository.findById(TOUR_ID)).thenReturn(Optional.of(tourDetail(BigDecimal.valueOf(100), null, 5)));
        when(tourDepartureRepository.findDepartureId(TOUR_ID, DATE)).thenReturn(Optional.of(DEPARTURE_ID));

        assertThatThrownBy(() -> service.createBooking(command(1L, 4, 2)))
                .isInstanceOf(BookingGroupTooLargeException.class);

        verify(tourDepartureRepository, never()).reserveSlots(any(), anyInt());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void groupSizeWithinMaxGuests_doesNotThrow() {
        service = newService();
        when(tourDetailRepository.findById(TOUR_ID)).thenReturn(Optional.of(tourDetail(BigDecimal.valueOf(100), null, 5)));
        when(tourDepartureRepository.findDepartureId(TOUR_ID, DATE)).thenReturn(Optional.of(DEPARTURE_ID));
        when(tourDepartureRepository.reserveSlots(DEPARTURE_ID, 5)).thenReturn(true);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> service.createBooking(command(1L, 5, 0))).doesNotThrowAnyException();
    }

    @Test
    void noMaxGuestsConfigured_skipsGroupSizeCheck() {
        service = newService();
        when(tourDetailRepository.findById(TOUR_ID)).thenReturn(Optional.of(tourDetail(BigDecimal.valueOf(100), null, null)));
        when(tourDepartureRepository.findDepartureId(TOUR_ID, DATE)).thenReturn(Optional.of(DEPARTURE_ID));
        when(tourDepartureRepository.reserveSlots(DEPARTURE_ID, 50)).thenReturn(true);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Booking result = service.createBooking(command(1L, 50, 0));

        assertThat(result).isNotNull();
    }

    @Test
    void noAvailableSlots_throwsNoAvailableSlotsException_neverSavesBooking() {
        service = newService();
        when(tourDetailRepository.findById(TOUR_ID)).thenReturn(Optional.of(tourDetail(BigDecimal.valueOf(100), null)));
        when(tourDepartureRepository.findDepartureId(TOUR_ID, DATE)).thenReturn(Optional.of(DEPARTURE_ID));
        when(tourDepartureRepository.reserveSlots(DEPARTURE_ID, 2)).thenReturn(false);

        assertThatThrownBy(() -> service.createBooking(command(1L, 2, 0)))
                .isInstanceOf(NoAvailableSlotsException.class);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void success_usesRegularPriceWhenNoDiscount_multipliesByGuestCount() {
        service = newService();
        when(tourDetailRepository.findById(TOUR_ID)).thenReturn(Optional.of(tourDetail(BigDecimal.valueOf(100), null)));
        when(tourDepartureRepository.findDepartureId(TOUR_ID, DATE)).thenReturn(Optional.of(DEPARTURE_ID));
        when(tourDepartureRepository.reserveSlots(DEPARTURE_ID, 3)).thenReturn(true);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Booking result = service.createBooking(command(7L, 2, 1));

        assertThat(result.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    void success_prefersDiscountPriceOverRegularPrice() {
        service = newService();
        when(tourDetailRepository.findById(TOUR_ID))
                .thenReturn(Optional.of(tourDetail(BigDecimal.valueOf(100), BigDecimal.valueOf(80))));
        when(tourDepartureRepository.findDepartureId(TOUR_ID, DATE)).thenReturn(Optional.of(DEPARTURE_ID));
        when(tourDepartureRepository.reserveSlots(DEPARTURE_ID, 2)).thenReturn(true);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Booking result = service.createBooking(command(7L, 2, 0));

        assertThat(result.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(160));
    }

    @Test
    void success_savesBookingWithUserIdFromCommand_pendingStatus_andCorrectDepartureId() {
        service = newService();
        when(tourDetailRepository.findById(TOUR_ID)).thenReturn(Optional.of(tourDetail(BigDecimal.valueOf(100), null)));
        when(tourDepartureRepository.findDepartureId(TOUR_ID, DATE)).thenReturn(Optional.of(DEPARTURE_ID));
        when(tourDepartureRepository.reserveSlots(eq(DEPARTURE_ID), anyInt())).thenReturn(true);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createBooking(command(99L, 1, 0));

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking saved = captor.getValue();
        assertThat(saved.userId()).isEqualTo(99L);
        assertThat(saved.tourId()).isEqualTo(TOUR_ID);
        assertThat(saved.departureId()).isEqualTo(DEPARTURE_ID);
        assertThat(saved.departureDate()).isEqualTo(DATE);
        assertThat(saved.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(saved.contactEmail()).isEqualTo("jane@example.com");
        assertThat(saved.idempotencyKey()).isEqualTo(IDEMPOTENCY_KEY);
    }
}
