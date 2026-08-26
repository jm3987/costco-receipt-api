package dev.shelfspace.receipts.model;

import java.time.LocalDate;

public record ReceiptSummary(
        int rowCount,
        int receiptCount,
        LocalDate firstPurchaseDate,
        LocalDate lastPurchaseDate
) {
}
