package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.TourDetail;

public interface GetTourDetailUseCase {
    TourDetail getTourDetail(Long id);
}
