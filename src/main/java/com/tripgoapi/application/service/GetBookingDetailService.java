package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.GetBookingDetailUseCase;
import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.domain.exception.BookingAccessDeniedException;
import com.tripgoapi.domain.exception.BookingNotFoundException;
import com.tripgoapi.domain.model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBookingDetailService implements GetBookingDetailUseCase {

    private final BookingRepositoryInterface bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingDetail(Long bookingId, Long requesterId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.userId().equals(requesterId)) {
            throw new BookingAccessDeniedException(bookingId);
        }

        return booking;
    }
}
