package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.GetDashboardStatsUseCase;
import com.tripgoapi.application.port.out.AdminStatsRepositoryInterface;
import com.tripgoapi.domain.model.DashboardStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetDashboardStatsService implements GetDashboardStatsUseCase {

    private final AdminStatsRepositoryInterface adminStatsRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        return adminStatsRepository.loadDashboardStats();
    }
}
