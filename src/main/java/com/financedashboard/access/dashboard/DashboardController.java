package com.financedashboard.access.dashboard;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardInsightsService dashboardInsightsService;

    public DashboardController(DashboardInsightsService dashboardInsightsService) {
        this.dashboardInsightsService = dashboardInsightsService;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public DashboardOverviewResponse overview(@RequestParam(defaultValue = "5") int recentActivityLimit) {
        return dashboardInsightsService.overview(recentActivityLimit);
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public List<MonthlyTrendResponse> trends(@RequestParam(defaultValue = "6") int months) {
        return dashboardInsightsService.monthlyTrends(months);
    }
}
