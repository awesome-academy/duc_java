package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.TourDepartureRepositoryInterface;
import com.tripgoapi.infrastructure.adapter.out.persistence.entity.TourDepartureEntity;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.TourDepartureJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TourDeparturePersistenceAdapter implements TourDepartureRepositoryInterface {

    private final TourDepartureJpaRepository tourDepartureJpaRepository;

    @Override
    public Optional<Long> findDepartureId(Long tourId, LocalDate departureDate) {
        return tourDepartureJpaRepository.findByTour_IdAndDepartureDate(tourId, departureDate)
                .map(TourDepartureEntity::getId);
    }

    @Override
    public boolean reserveSlots(Long departureId, int guestCount) {
        return tourDepartureJpaRepository.reserveSlotsIfAvailable(departureId, guestCount) > 0;
    }
}
