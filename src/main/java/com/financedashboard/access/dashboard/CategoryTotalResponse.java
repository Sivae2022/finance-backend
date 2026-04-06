package com.financedashboard.access.dashboard;

import com.financedashboard.access.finance.FinancialRecordType;

import java.math.BigDecimal;

public record CategoryTotalResponse(
        String category,
        FinancialRecordType type,
        BigDecimal total
) {
}
