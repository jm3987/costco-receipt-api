package dev.shelfspace.receipts.purchase.model;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * Represents one historical purchase of an item
 *
 * Receipt context is included so API consumers do not need to make an
 * additional receipt requests for each search result.
 */
public record ItemPurchase(
        String orderNumber,
        LocalDate transactionDate,
        String receiptType,
        String warehouseInfo,
        String itemSku,
        String itemName,
        String itemActualName,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        BigDecimal instantSavings
) {
}
