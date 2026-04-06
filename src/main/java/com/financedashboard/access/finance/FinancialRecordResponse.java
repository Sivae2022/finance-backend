package com.financedashboard.access.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record FinancialRecordResponse(
        Long id,
        BigDecimal amount,
        FinancialRecordType type,
        String category,
        LocalDate recordDate,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
