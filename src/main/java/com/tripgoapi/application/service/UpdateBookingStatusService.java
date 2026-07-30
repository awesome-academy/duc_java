package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.UpdateBookingStatusUseCase;
import com.tripgoapi.application.port.out.AdminBookingRepositoryInterface;
import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.application.port.out.TourDepartureRepositoryInterface;
import com.tripgoapi.domain.exception.BookingNotFoundException;
import com.tripgoapi.domain.exception.BookingStatusTransitionNotAllowedException;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateBookingStatusService implements UpdateBookingStatusUseCase {

    private final BookingRepositoryInterface bookingRepository;
    private final AdminBookingRepositoryInterface adminBookingRepository;
    private final TourDepartureRepositoryInterface tourDepartureRepository;

    @Override
    @Transactional
    public Booking updateStatus(Long bookingId, BookingStatus targetStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        switch (targetStatus) {
            case CONFIRMED -> confirm(bookingId, targetStatus);
            case CANCELLED -> cancel(booking, targetStatus);
            // COMPLETED is driven by the departure date passing, not by an admin click, and
            // nothing may move a booking back to PENDING.
            default -> throw new BookingStatusTransitionNotAllowedException(booking.status(), targetStatus);
        }

        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    private void confirm(Long bookingId, BookingStatus targetStatus) {
        if (!adminBookingRepository.confirmIfPending(bookingId)) {
            throw new BookingStatusTransitionNotAllowedException(currentStatus(bookingId), targetStatus);
        }
    }

    private void cancel(Booking booking, BookingStatus targetStatus) {
        // Same atomic guard the customer-facing cancel uses: whoever loses the race gets false
        // here instead of both callers releasing the same slots twice.
        if (!bookingRepository.cancelIfCancellable(booking.id())) {
            throw new BookingStatusTransitionNotAllowedException(currentStatus(booking.id()), targetStatus);
        }
        tourDepartureRepository.releaseSlots(booking.departureId(), booking.adults() + booking.children());
    }

    /**
     * Re-read rather than reuse the snapshot taken before the atomic update: for a request that
     * lost a race, that snapshot still reports the stale pre-transition status.
     */
    private BookingStatus currentStatus(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId))
                .status();
    }
}
