package com.tripgoapi.application.port.in;

public interface UpdateTourUseCase {

    /**
     * @throws com.tripgoapi.domain.exception.TourNotFoundException if no such tour exists, or it
     *                                                              has been soft-deleted
     */
    void updateTour(Long id, SaveTourCommand command);
}
