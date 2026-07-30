package com.tripgoapi.infrastructure.adapter.in.admin;

import com.tripgoapi.application.port.in.AdminBookingSearchQuery;
import com.tripgoapi.application.port.in.GetAdminBookingsUseCase;
import com.tripgoapi.application.port.in.GetDashboardStatsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private static final int RECENT_BOOKINGS = 5;

    private final GetDashboardStatsUseCase getDashboardStatsUseCase;
    private final GetAdminBookingsUseCase getAdminBookingsUseCase;

    @GetMapping({"/admin", "/admin/"})
    public String dashboard(Model model) {
        model.addAttribute("stats", getDashboardStatsUseCase.getStats());
        model.addAttribute("recentBookings",
                getAdminBookingsUseCase.searchBookings(new AdminBookingSearchQuery(null, 1, RECENT_BOOKINGS)).data());
        model.addAttribute("activeMenu", "dashboard");
        return "admin/dashboard";
    }
}
