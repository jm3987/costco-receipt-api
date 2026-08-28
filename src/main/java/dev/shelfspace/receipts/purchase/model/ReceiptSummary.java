package dev.shelfspace.receipts.model;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * Provides collection-level statistics for the available receipt data.
 *
 * @param rowCount
 * @param receiptCount
 * @param firstPurchaseDate
 * @param lastPurchaseDate
 * @param totalSpending
 */
public record ReceiptSummary(
        int rowCount,
        int receiptCount,
        LocalDate firstPurchaseDate,
        LocalDate lastPurchaseDate,
        BigDecimal totalSpending
) {
}
