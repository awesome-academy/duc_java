package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.GetTourDetailUseCase;
import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.TourDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTourDetailService implements GetTourDetailUseCase {

    private final TourDetailRepositoryInterface tourDetailRepository;

    @Override
    public TourDetail getTourDetail(Long id) {
        return tourDetailRepository.findById(id)
                .orElseThrow(() -> new TourNotFoundException(id));
    }
}
