package dev.shelfspace.receipts.purchase.model;


import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Compact representation used to identify a receipt before requesting
 * its complete item-level details
 *
 * Warehouse information may be absent when it does not apply to the
 * receipt type, such as an online transaction
 */
public record ReceiptOverview(
        String orderNumber,
        LocalDate transactionDate,
        String receiptType,
        String warehouseInfo,
        BigDecimal finalTotal
) {
}
