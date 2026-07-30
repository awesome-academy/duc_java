package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.DashboardStats;

public interface GetDashboardStatsUseCase {

    DashboardStats getStats();
}
