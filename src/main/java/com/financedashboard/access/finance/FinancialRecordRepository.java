package com.financedashboard.access.finance;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    @Query("select coalesce(sum(fr.amount), 0) from FinancialRecord fr where fr.type = :type")
    BigDecimal sumAmountByType(@Param("type") FinancialRecordType type);

    List<FinancialRecord> findByRecordDateGreaterThanEqualOrderByRecordDateAscCreatedAtAsc(LocalDate recordDate);

    List<FinancialRecord> findByOrderByRecordDateDescCreatedAtDesc(Pageable pageable);

    interface CategoryTotalView {
        String getCategory();

        FinancialRecordType getType();

        BigDecimal getTotal();
    }

    @Query("""
            select fr.category as category, fr.type as type, coalesce(sum(fr.amount), 0) as total
            from FinancialRecord fr
            group by fr.category, fr.type
            order by fr.type asc, total desc, fr.category asc
            """)
    List<CategoryTotalView> findCategoryTotals();

    default List<FinancialRecord> findAll(Specification<FinancialRecord> specification) {
        return findAll(specification, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("recordDate"),
                org.springframework.data.domain.Sort.Order.desc("createdAt")
        ));
    }
}
