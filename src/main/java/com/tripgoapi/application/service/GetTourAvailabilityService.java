package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.GetTourAvailabilityUseCase;
import com.tripgoapi.application.port.out.TourAvailabilityRepositoryInterface;
import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.TourAvailability;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTourAvailabilityService implements GetTourAvailabilityUseCase {

    private final TourDetailRepositoryInterface tourDetailRepository;
    private final TourAvailabilityRepositoryInterface availabilityRepository;

    @Override
    public List<TourAvailability> getAvailability(Long tourId, YearMonth month) {
        if (!tourDetailRepository.existsActiveTour(tourId)) {
            throw new TourNotFoundException(tourId);
        }
        return availabilityRepository.findAvailability(tourId, month);
    }
}
