package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.DashboardStats;

public interface AdminStatsRepositoryInterface {

    DashboardStats loadDashboardStats();
}
