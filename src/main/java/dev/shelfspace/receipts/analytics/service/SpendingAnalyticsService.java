package dev.shelfspace.receipts.analytics.service;

import dev.shelfspace.receipts.analytics.model.MonthlySpending;
import dev.shelfspace.receipts.analytics.model.SpendingAnalyticsQuery;
import dev.shelfspace.receipts.purchase.model.ReceiptDetail;
import dev.shelfspace.receipts.purchase.repository.ReceiptRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

    private BigDecimal calculateNetSpending(List<ReceiptDetail> receipts) {
        // ReceiptDetail represents one complete receipt, so finalTotal
        // is counted once even when that receipt contains many items.
        return receipts.stream()
                .map(ReceiptDetail::finalTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
