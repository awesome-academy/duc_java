package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.GetToursUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.TourSearchQuery;
import com.tripgoapi.application.port.out.TourRepositoryInterface;
import com.tripgoapi.domain.model.Tour;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetToursService implements GetToursUseCase {

    private final TourRepositoryInterface tourRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResult<Tour> searchTours(TourSearchQuery query) {
        return tourRepository.searchTours(query);
    }
}
