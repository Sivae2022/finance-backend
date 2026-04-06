package com.financedashboard.access.finance;

import com.financedashboard.access.common.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;

    public FinancialRecordService(FinancialRecordRepository financialRecordRepository) {
        this.financialRecordRepository = financialRecordRepository;
    }

    public FinancialRecordResponse createRecord(CreateFinancialRecordRequest request) {
        FinancialRecord record = new FinancialRecord();
        apply(record, request.amount(), request.type(), request.category(), request.recordDate(), request.description());
        return toResponse(financialRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<FinancialRecordResponse> listRecords(
            FinancialRecordType type,
            String category,
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
        validateFilters(fromDate, toDate, minAmount, maxAmount);
        return financialRecordRepository.findAll(buildSpecification(type, category, fromDate, toDate, minAmount, maxAmount))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecord(Long id) {
        return toResponse(findRecord(id));
    }

    public FinancialRecordResponse updateRecord(Long id, UpdateFinancialRecordRequest request) {
        FinancialRecord record = findRecord(id);
        apply(record, request.amount(), request.type(), request.category(), request.recordDate(), request.description());
        return toResponse(financialRecordRepository.save(record));
    }

    public void deleteRecord(Long id) {
        financialRecordRepository.delete(findRecord(id));
    }

    public FinancialRecordResponse toResponse(FinancialRecord record) {
        return new FinancialRecordResponse(
                record.getId(),
                record.getAmount(),
                record.getType(),
                record.getCategory(),
                record.getRecordDate(),
                record.getDescription(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private FinancialRecord findRecord(Long id) {
        return financialRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record with id %d was not found".formatted(id)));
    }

    private void apply(
            FinancialRecord record,
            BigDecimal amount,
            FinancialRecordType type,
            String category,
            LocalDate recordDate,
            String description
    ) {
        record.setAmount(amount);
        record.setType(type);
        record.setCategory(category.trim());
        record.setRecordDate(recordDate);
        record.setDescription(description == null || description.isBlank() ? null : description.trim());
    }

    private Specification<FinancialRecord> buildSpecification(
            FinancialRecordType type,
            String category,
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("category")), category.trim().toLowerCase()));
            }
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("recordDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("recordDate"), toDate));
            }
            if (minAmount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validateFilters(LocalDate fromDate, LocalDate toDate, BigDecimal minAmount, BigDecimal maxAmount) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("minAmount cannot be greater than maxAmount");
        }
    }
}
