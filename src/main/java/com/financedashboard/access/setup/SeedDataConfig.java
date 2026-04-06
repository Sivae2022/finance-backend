package com.financedashboard.access.setup;

import com.financedashboard.access.finance.FinancialRecord;
import com.financedashboard.access.finance.FinancialRecordRepository;
import com.financedashboard.access.finance.FinancialRecordType;
import com.financedashboard.access.user.AppUser;
import com.financedashboard.access.user.AppUserRepository;
import com.financedashboard.access.user.UserRole;
import com.financedashboard.access.user.UserStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Configuration
public class SeedDataConfig {

    @Bean
    ApplicationRunner seedInitialData(
            AppUserRepository appUserRepository,
            FinancialRecordRepository financialRecordRepository,
            PasswordEncoder passwordEncoder,
            @Value("${application.bootstrap.seed-data:true}") boolean seedData
    ) {
        return args -> {
            if (!seedData) {
                return;
            }

            if (appUserRepository.count() == 0) {
                appUserRepository.saveAll(List.of(
                        user("admin.priya", "Priya Nair", "Admin@123", UserRole.ADMIN, UserStatus.ACTIVE, passwordEncoder),
                        user("analyst.rahul", "Rahul Verma", "Analyst@123", UserRole.ANALYST, UserStatus.ACTIVE, passwordEncoder),
                        user("viewer.meera", "Meera Iyer", "Viewer@123", UserRole.VIEWER, UserStatus.ACTIVE, passwordEncoder),
                        user("archived.aisha", "Aisha Khan", "Inactive@123", UserRole.ANALYST, UserStatus.INACTIVE, passwordEncoder)
                ));
            }

            if (financialRecordRepository.count() == 0) {
                LocalDate today = LocalDate.now();
                financialRecordRepository.saveAll(List.of(
                        record("6500.00", FinancialRecordType.INCOME, "Salary", today.minusDays(1), "Primary salary credit"),
                        record("1800.00", FinancialRecordType.EXPENSE, "Rent", today.minusDays(2), "Apartment rent payment"),
                        record("240.50", FinancialRecordType.EXPENSE, "Groceries", today.minusDays(3), "Weekly grocery run"),
                        record("1200.00", FinancialRecordType.INCOME, "Consulting", today.minusDays(5), "Advisory engagement payout"),
                        record("180.75", FinancialRecordType.EXPENSE, "Utilities", today.minusDays(8), "Electricity and internet"),
                        record("420.30", FinancialRecordType.EXPENSE, "Travel", today.minusDays(20), "Client visit transit"),
                        record("900.00", FinancialRecordType.INCOME, "Bonus", today.minusDays(32), "Quarterly performance bonus"),
                        record("95.00", FinancialRecordType.EXPENSE, "Software", today.minusDays(40), "Budgeting tool subscription")
                ));
            }
        };
    }

    private AppUser user(
            String username,
            String fullName,
            String rawPassword,
            UserRole role,
            UserStatus status,
            PasswordEncoder passwordEncoder
    ) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    private FinancialRecord record(
            String amount,
            FinancialRecordType type,
            String category,
            LocalDate date,
            String description
    ) {
        FinancialRecord record = new FinancialRecord();
        record.setAmount(new BigDecimal(amount));
        record.setType(type);
        record.setCategory(category);
        record.setRecordDate(date);
        record.setDescription(description);
        return record;
    }
}
