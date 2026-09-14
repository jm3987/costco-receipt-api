package dev.shelfspace.receipts.analytics.model;

import java.math.BigDecimal;
import java.util.List;

/** Range-wide spending total together with its purchase-type breakdown. */
public record SpendingByTypeSummary(
        BigDecimal grandTotalSpending,
        List<SpendingByType> breakdown
) {
}
