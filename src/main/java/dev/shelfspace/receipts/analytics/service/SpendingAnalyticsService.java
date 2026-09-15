package dev.shelfspace.receipts.analytics.service;

import dev.shelfspace.receipts.analytics.model.MonthlySpending;
import dev.shelfspace.receipts.analytics.model.MonthlySpendingByType;
import dev.shelfspace.receipts.analytics.model.SpendingByType;
import dev.shelfspace.receipts.analytics.model.SpendingByTypeSummary;
import dev.shelfspace.receipts.analytics.model.SpendingAnalyticsQuery;
import dev.shelfspace.receipts.purchase.model.ReceiptDetail;
import dev.shelfspace.receipts.purchase.model.PurchaseType;
import dev.shelfspace.receipts.purchase.repository.ReceiptRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class SpendingAnalyticsService {
    private final ReceiptRepository receiptRepository;

    @Inject
    public SpendingAnalyticsService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    /**
     * Calculates net spending by month for the requested inclusive date range.
     */
    public List<MonthlySpending> findMonthlySpending(SpendingAnalyticsQuery query) {
        Map<YearMonth, List<ReceiptDetail>> receiptsByMonth = receiptRepository.findAll().stream()
                .filter(receipt -> query.includes(receipt.transactionDate()))
                .collect(Collectors.groupingBy(
                        receipt -> YearMonth.from(receipt.transactionDate()),
                        // Chronological ordering is part of the API contract
                        // because chart consumers rely on it.
                        TreeMap::new,
                        Collectors.toList()
                ));

        return receiptsByMonth.entrySet()
                .stream()
                .map(entry -> new MonthlySpending(
                        entry.getKey(),
                        entry.getValue().size(),
                        calculateNetSpending(entry.getValue())
                ))
                .toList();
    }

    /** Calculates one total per purchase type and a grand total for the inclusive range. */
    public SpendingByTypeSummary findSpendingByType(SpendingAnalyticsQuery query) {
        Map<PurchaseType, List<ReceiptDetail>> receiptsByType = new EnumMap<>(PurchaseType.class);
        for (PurchaseType type : PurchaseType.values()) {
            receiptsByType.put(type, new ArrayList<>());
        }

        for (ReceiptDetail receipt : receiptRepository.findAll()) {
            if (query.includes(receipt.transactionDate())) {
                receiptsByType.get(PurchaseType.fromValue(receipt.receiptType())).add(receipt);
            }
        }

        List<SpendingByType> breakdown = receiptsByType.entrySet().stream()
                .map(entry -> new SpendingByType(
                        entry.getKey().value(),
                        entry.getValue().size(),
                        calculateNetSpending(entry.getValue())
                ))
                .toList();

        BigDecimal grandTotalSpending = breakdown.stream()
                .map(SpendingByType::totalSpending)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SpendingByTypeSummary(grandTotalSpending, breakdown);
    }

    public List<MonthlySpendingByType> findMonthlySpendingByType(
            SpendingAnalyticsQuery query
    ){
        Map<YearMonth, Map<PurchaseType, List<ReceiptDetail> >> receiptsByMonthAndType =
                new TreeMap<>();

        for (ReceiptDetail receipt : receiptRepository.findAll()) {
            if (!query.includes(receipt.transactionDate())){
                continue;
            }

            YearMonth month = YearMonth.from(receipt.transactionDate());
            PurchaseType type = PurchaseType.fromValue(receipt.receiptType());

            Map<PurchaseType, List<ReceiptDetail>> receiptsByType =
                    receiptsByMonthAndType.computeIfAbsent(
                            month, ignored -> new EnumMap<>(PurchaseType.class)
                    );

            receiptsByType.computeIfAbsent(type, ignored -> new ArrayList<>())
                    .add(receipt);
        }

        List<MonthlySpendingByType> results = new ArrayList<>();
        for (var monthEntry : receiptsByMonthAndType.entrySet()) {
            for (var typeEntry : monthEntry.getValue().entrySet()) {
                List<ReceiptDetail> receipts = typeEntry.getValue();
                results.add(new MonthlySpendingByType(
                        monthEntry.getKey(),
                        typeEntry.getKey().value(),
                        receipts.size(),
                        calculateNetSpending(receipts)
                ));
            }

        }
        return results;
    }

    private BigDecimal calculateNetSpending(List<ReceiptDetail> receipts) {
        // ReceiptDetail represents one complete receipt, so finalTotal
        // is counted once even when that receipt contains many items.
        return receipts.stream()
                .map(ReceiptDetail::finalTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
