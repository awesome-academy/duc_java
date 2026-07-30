package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.AdminStatsRepositoryInterface;
import com.tripgoapi.domain.model.BookingStatus;
import com.tripgoapi.domain.model.DashboardStats;
import com.tripgoapi.domain.model.Role;
import com.tripgoapi.domain.model.TourStatus;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.BookingJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.DestinationJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourJpaRepository;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AdminStatsPersistenceAdapter implements AdminStatsRepositoryInterface {

    private final TourJpaRepository tourJpaRepository;
    private final DestinationJpaRepository destinationJpaRepository;
    private final BookingJpaRepository bookingJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public DashboardStats loadDashboardStats() {
        BigDecimal revenue = bookingJpaRepository.sumRealizedRevenue();

        return new DashboardStats(
                tourJpaRepository.countByStatus(TourStatus.ACTIVE.name()),
                destinationJpaRepository.count(),
                bookingJpaRepository.countByStatus(BookingStatus.PENDING.name()),
                bookingJpaRepository.countByStatus(BookingStatus.CONFIRMED.name()),
                userJpaRepository.countByRole(Role.USER.name()),
                revenue == null ? BigDecimal.ZERO : revenue
        );
    }
}
