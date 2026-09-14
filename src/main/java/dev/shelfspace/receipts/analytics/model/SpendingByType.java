package dev.shelfspace.receipts.analytics.model;

import java.math.BigDecimal;

/** Net spending for one purchase type in the selected date range. */
public record SpendingByType(
        String purchaseType,
        int receiptCount,
        BigDecimal totalSpending
) {
}
