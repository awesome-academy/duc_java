package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.GetBookingsUseCase;
import com.tripgoapi.application.port.out.BookingRepositoryInterface;
import com.tripgoapi.domain.model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetBookingsService implements GetBookingsUseCase {

    private final BookingRepositoryInterface bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsForUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }
}
