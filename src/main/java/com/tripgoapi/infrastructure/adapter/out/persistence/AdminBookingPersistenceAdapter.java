package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.in.AdminBookingSearchQuery;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.out.AdminBookingRepositoryInterface;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.BookingEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.BookingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminBookingPersistenceAdapter implements AdminBookingRepositoryInterface {

    private final BookingJpaRepository bookingJpaRepository;

    @Override
    public PageResult<Booking> searchBookings(AdminBookingSearchQuery query) {
        // Unsorted PageRequest on purpose: the ORDER BY lives in the query itself, and adding a
        // Sort here would append a second, conflicting ordering.
        PageRequest pageRequest = PageRequest.of(query.page() - 1, query.size());
        Page<BookingEntity> page = bookingJpaRepository.searchForAdmin(
                query.status() == null ? null : query.status().name(), pageRequest);

        return new PageResult<>(
                page.getContent().stream().map(BookingEntityMapper::toDomain).toList(),
                page.getTotalElements(),
                query.page(),
                query.size()
        );
    }

    @Override
    public boolean confirmIfPending(Long id) {
        return bookingJpaRepository.confirmIfPending(id) > 0;
    }
}
