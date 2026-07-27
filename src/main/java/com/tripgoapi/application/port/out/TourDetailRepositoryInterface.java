package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.TourDetail;

import java.math.BigDecimal;
import java.util.Optional;

public interface TourDetailRepositoryInterface {

    Optional<TourDetail> findById(Long id);

    boolean existsActiveTour(Long id);

    Optional<BigDecimal> findRatingAvg(Long id);
}
