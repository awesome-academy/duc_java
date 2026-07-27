package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Tour;

public interface GetToursUseCase {
    PageResult<Tour> searchTours(TourSearchQuery query);
}
