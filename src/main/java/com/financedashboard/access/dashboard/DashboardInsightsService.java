package com.financedashboard.access.dashboard;

import com.financedashboard.access.finance.FinancialRecord;
import com.financedashboard.access.finance.FinancialRecordRepository;
import com.financedashboard.access.finance.FinancialRecordResponse;
import com.financedashboard.access.finance.FinancialRecordService;
import com.financedashboard.access.finance.FinancialRecordType;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardInsightsService {

    private final FinancialRecordRepository financialRecordRepository;
    private final FinancialRecordService financialRecordService;

    public DashboardInsightsService(
            FinancialRecordRepository financialRecordRepository,
            FinancialRecordService financialRecordService
    ) {
        this.financialRecordRepository = financialRecordRepository;
        this.financialRecordService = financialRecordService;
    }

    public DashboardOverviewResponse overview(int recentActivityLimit) {
        if (recentActivityLimit < 1 || recentActivityLimit > 20) {
            throw new IllegalArgumentException("recentActivityLimit must be between 1 and 20");
        }

        BigDecimal totalIncome = financialRecordRepository.sumAmountByType(FinancialRecordType.INCOME);
        BigDecimal totalExpenses = financialRecordRepository.sumAmountByType(FinancialRecordType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        List<CategoryTotalResponse> categoryTotals = financialRecordRepository.findCategoryTotals().stream()
                .map(view -> new CategoryTotalResponse(view.getCategory(), view.getType(), view.getTotal()))
                .toList();

        List<FinancialRecordResponse> recentActivity = financialRecordRepository
                .findByOrderByRecordDateDescCreatedAtDesc(PageRequest.of(0, recentActivityLimit))
                .stream()
                .map(financialRecordService::toResponse)
                .toList();

        return new DashboardOverviewResponse(totalIncome, totalExpenses, netBalance, categoryTotals, recentActivity);
    }

    public List<MonthlyTrendResponse> monthlyTrends(int months) {
        if (months < 1 || months > 24) {
            throw new IllegalArgumentException("months must be between 1 and 24");
        }

        YearMonth currentMonth = YearMonth.now();
        YearMonth startingMonth = currentMonth.minusMonths(months - 1L);
        LocalDate startingDate = startingMonth.atDay(1);

        Map<YearMonth, TotalsAccumulator> monthlyTotals = new LinkedHashMap<>();
        for (int offset = 0; offset < months; offset++) {
            monthlyTotals.put(startingMonth.plusMonths(offset), new TotalsAccumulator());
        }

        for (FinancialRecord record : financialRecordRepository.findByRecordDateGreaterThanEqualOrderByRecordDateAscCreatedAtAsc(startingDate)) {
            YearMonth month = YearMonth.from(record.getRecordDate());
            TotalsAccumulator accumulator = monthlyTotals.get(month);
            if (accumulator == null) {
                continue;
            }
            if (record.getType() == FinancialRecordType.INCOME) {
                accumulator.income = accumulator.income.add(record.getAmount());
            } else {
                accumulator.expenses = accumulator.expenses.add(record.getAmount());
            }
        }

        List<MonthlyTrendResponse> response = new ArrayList<>();
        monthlyTotals.forEach((month, totals) -> response.add(new MonthlyTrendResponse(
                month,
                totals.income,
                totals.expenses,
                totals.income.subtract(totals.expenses)
        )));
        return response;
    }

    private static final class TotalsAccumulator {
        private BigDecimal income = BigDecimal.ZERO;
        private BigDecimal expenses = BigDecimal.ZERO;
    }
}
