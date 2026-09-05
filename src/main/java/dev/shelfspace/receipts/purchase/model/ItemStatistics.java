package dev.shelfspace.receipts.purchase.model;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * Summarizes historical purchases for one exact receipt SKU.
 *
 * @param sku
 * @param displayName
 * @param receiptCount
 * @param firstPurchaseDate
 * @param lastPurchaseDate
 * @param lowestPrice
 * @param highestPrice
 * @param averagePrice
 * @param latestPrice
 */
public record ItemStatistics(
        String sku,
        String displayName,
        long receiptCount,
        LocalDate firstPurchaseDate,
        LocalDate lastPurchaseDate,
        BigDecimal lowestPrice,
        BigDecimal highestPrice,
        BigDecimal averagePrice,
        BigDecimal latestPrice
) {
}
