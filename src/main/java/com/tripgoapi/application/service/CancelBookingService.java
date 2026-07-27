package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.CancelBookingUseCase;
import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.application.port.out.TourDepartureRepositoryInterface;
import com.tripgoapi.domain.exception.BookingAccessDeniedException;
import com.tripgoapi.domain.exception.BookingCancellationNotAllowedException;
import com.tripgoapi.domain.exception.BookingNotFoundException;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelBookingService implements CancelBookingUseCase {

    private final BookingRepositoryInterface bookingRepository;
    private final TourDepartureRepositoryInterface tourDepartureRepository;

    @Override
    @Transactional
    public Booking cancelBooking(Long bookingId, Long requesterId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.userId().equals(requesterId)) {
            throw new BookingAccessDeniedException(bookingId);
        }

        // Atomic guard: only one of several concurrent cancel requests for this booking can
        // flip PENDING/CONFIRMED -> CANCELLED; a losing request gets false here instead of both
        // passing a stale in-memory status check and double-releasing slots.
        if (!bookingRepository.cancelIfCancellable(bookingId)) {
            // Re-read rather than reuse `booking.status()`: that snapshot was taken before
            // cancelIfCancellable ran, so for a request that lost the race it would still say
            // PENDING/CONFIRMED even though the real, current status is CANCELLED/COMPLETED.
            BookingStatus currentStatus = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new BookingNotFoundException(bookingId))
                    .status();
            throw new BookingCancellationNotAllowedException(currentStatus);
        }

        tourDepartureRepository.releaseSlots(booking.departureId(), booking.adults() + booking.children());

        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }
}
