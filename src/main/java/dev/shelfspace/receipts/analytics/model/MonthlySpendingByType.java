package dev.shelfspace.receipts.analytics.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlySpendingByType(
        YearMonth month,
        String purchaseType,
        int receiptCount,
        BigDecimal totalSpending
) {
}
