package dev.shelfspace.receipts.purchase.model;

import com.aayushatharva.brotli4j.common.annotations.Local;

import java.time.LocalDate;

/**
 * Defines the filtering and pagination options used when browsing receipts.
 *
 * <p>Null filter values represent unrestricted criteria. For example, a null
 * purchase type includes receipts from every supported sales channel.</p>
 */
public record ReceiptQuery(
        LocalDate from,
        LocalDate to,
        PurchaseType type,
        int page,
        int size
) {
}
