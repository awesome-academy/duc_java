package com.tripgoapi.application.port.in;

public interface DeleteTourUseCase {

    /**
     * Soft-deletes the tour: it disappears from the admin list and from the public API, but its
     * bookings and reviews keep pointing at an intact row.
     *
     * @throws com.tripgoapi.domain.exception.TourNotFoundException if no such tour exists, or it
     *                                                              was already deleted
     */
    void deleteTour(Long id);
}
