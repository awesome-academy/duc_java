package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.TourAvailabilityRepositoryInterface;
import com.tripgoapi.domain.model.TourAvailability;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourDepartureJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TourAvailabilityPersistenceAdapter implements TourAvailabilityRepositoryInterface {

    private final TourDepartureJpaRepository tourDepartureJpaRepository;

    @Override
    public List<TourAvailability> findAvailability(Long tourId, YearMonth month) {
        return tourDepartureJpaRepository
                .findByTour_IdAndDepartureDateBetweenOrderByDepartureDateAsc(
                        tourId, month.atDay(1), month.atEndOfMonth())
                .stream()
                .map(d -> new TourAvailability(d.getDepartureDate(), d.getTotalSlots(), d.getBookedSlots()))
                .toList();
    }
}
