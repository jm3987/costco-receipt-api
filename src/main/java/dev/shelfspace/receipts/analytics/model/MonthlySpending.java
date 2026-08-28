package dev.shelfspace.receipts.analytics.model;


import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Represents net receipt spending for one calendar month.
 *
 * <p>Refunds and returns have negative totals and therefore reduces totalspending</p>
 */
public record MonthlySpending(
        YearMonth month,
        int receiptCount,
        BigDecimal totalSpending
) {
}
