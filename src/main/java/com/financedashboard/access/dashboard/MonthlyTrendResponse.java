package com.financedashboard.access.dashboard;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyTrendResponse(
        YearMonth month,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal net
) {
}
