package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.AdminTourSummary;

public interface GetAdminToursUseCase {

    PageResult<AdminTourSummary> searchTours(AdminTourSearchQuery query);
}
