package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.AdminBookingSearchQuery;
import com.tripgoapi.application.port.in.GetAdminBookingsUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.out.AdminBookingRepositoryInterface;
import com.tripgoapi.domain.model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAdminBookingsService implements GetAdminBookingsUseCase {

    private final AdminBookingRepositoryInterface adminBookingRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResult<Booking> searchBookings(AdminBookingSearchQuery query) {
        return adminBookingRepository.searchBookings(query);
    }
}
