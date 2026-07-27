package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.CreateBookingCommand;
import com.tripgoapi.application.port.in.CreateBookingUseCase;
import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.application.port.out.TourDepartureRepositoryInterface;
import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.domain.exception.BookingGroupTooLargeException;
import com.tripgoapi.domain.exception.NoAvailableSlotsException;
import com.tripgoapi.domain.exception.TourDepartureNotFoundException;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.TourDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateBookingService implements CreateBookingUseCase {

    private final TourDetailRepositoryInterface tourDetailRepository;
    private final TourDepartureRepositoryInterface tourDepartureRepository;
    private final BookingRepositoryInterface bookingRepository;

    @Override
    @Transactional
    public Booking createBooking(CreateBookingCommand command) {
        // Idempotency: a retried request (client timeout/double-submit) with the same key
        // returns the already-created booking instead of reserving slots a second time.
        Optional<Booking> existing = bookingRepository.findByIdempotencyKey(command.idempotencyKey());
        if (existing.isPresent()) {
            return existing.get();
        }

        TourDetail tour = tourDetailRepository.findById(command.tourId())
                .orElseThrow(() -> new TourNotFoundException(command.tourId()));

        Long departureId = tourDepartureRepository.findDepartureId(command.tourId(), command.date())
                .orElseThrow(() -> new TourDepartureNotFoundException(command.tourId(), command.date()));

        int guestCount = command.adults() + command.children();
        if (tour.maxGuests() != null && guestCount > tour.maxGuests()) {
            throw new BookingGroupTooLargeException(guestCount, tour.maxGuests());
        }

        if (!tourDepartureRepository.reserveSlots(departureId, guestCount)) {
            throw new NoAvailableSlotsException();
        }

        BigDecimal unitPrice = tour.discountPrice() != null ? tour.discountPrice() : tour.price();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(guestCount));

        Booking booking = Booking.pending(
                command.idempotencyKey(),
                command.userId(),
                tour,
                departureId,
                command.date(),
                command.adults(),
                command.children(),
                totalPrice,
                command.contactName(),
                command.contactEmail(),
                command.contactPhone()
        );

        return bookingRepository.save(booking);
    }
}
