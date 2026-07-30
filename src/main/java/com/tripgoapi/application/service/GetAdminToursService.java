package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.AdminTourSearchQuery;
import com.tripgoapi.application.port.in.GetAdminTourDetailUseCase;
import com.tripgoapi.application.port.in.GetAdminToursUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.out.AdminTourRepositoryInterface;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.AdminTourDetail;
import com.tripgoapi.domain.model.AdminTourSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAdminToursService implements GetAdminToursUseCase, GetAdminTourDetailUseCase {

    private final AdminTourRepositoryInterface adminTourRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResult<AdminTourSummary> searchTours(AdminTourSearchQuery query) {
        return adminTourRepository.searchTours(query);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTourDetail getTourForEdit(Long id) {
        return adminTourRepository.findById(id)
                .orElseThrow(() -> new TourNotFoundException(id));
    }
}
