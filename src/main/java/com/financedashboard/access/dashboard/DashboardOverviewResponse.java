package com.financedashboard.access.dashboard;

import com.financedashboard.access.finance.FinancialRecordResponse;

import java.math.BigDecimal;
import java.util.List;

public record DashboardOverviewResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        List<CategoryTotalResponse> categoryTotals,
        List<FinancialRecordResponse> recentActivity
) {
}
