package dev.shelfspace.receipts;

import java.time.LocalDate;

public record ReceiptSummary(
        int rowCount,
        int receiptCount,
        LocalDate firstPurchaseDate,
        LocalDate lastPurchaseDate
) {
}
