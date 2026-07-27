package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.TourAvailability;

import java.time.YearMonth;
import java.util.List;

public interface TourAvailabilityRepositoryInterface {
    List<TourAvailability> findAvailability(Long tourId, YearMonth month);
}
