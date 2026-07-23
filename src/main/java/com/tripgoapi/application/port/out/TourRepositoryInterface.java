package com.tripgoapi.application.port.out;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.TourSearchQuery;
import com.tripgoapi.domain.model.Tour;

public interface TourRepositoryInterface {
    PageResult<Tour> searchTours(TourSearchQuery query);
}
