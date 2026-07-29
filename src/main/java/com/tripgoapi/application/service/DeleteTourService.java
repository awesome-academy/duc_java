package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.DeleteTourUseCase;
import com.tripgoapi.application.port.out.AdminTourRepositoryInterface;
import com.tripgoapi.domain.exception.TourNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteTourService implements DeleteTourUseCase {

    private final AdminTourRepositoryInterface adminTourRepository;

    @Override
    @Transactional
    public void deleteTour(Long id) {
        // Soft delete only: the uploaded image files are deliberately left on disk. A DELETED tour
        // is still referenced by past bookings whose detail pages render its thumbnail, so the
        // files must outlive the delete action.
        if (!adminTourRepository.softDeleteTour(id)) {
            throw new TourNotFoundException(id);
        }
    }
}
