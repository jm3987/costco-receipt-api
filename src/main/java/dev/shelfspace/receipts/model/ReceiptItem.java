package dev.shelfspace.receipts.model;

import java.math.BigDecimal;

public record ReceiptItem(
        String itemSku,
        String itemName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        BigDecimal instantSavings
) {
}
