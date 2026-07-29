package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.AdminTourDetail;

public interface GetAdminTourDetailUseCase {

    /**
     * @throws com.tripgoapi.domain.exception.TourNotFoundException if no such tour exists, or it
     *                                                              has been soft-deleted
     */
    AdminTourDetail getTourForEdit(Long id);
}
