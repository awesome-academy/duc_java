package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.TourAvailability;

import java.time.YearMonth;
import java.util.List;

public interface GetTourAvailabilityUseCase {
    List<TourAvailability> getAvailability(Long tourId, YearMonth month);
}
