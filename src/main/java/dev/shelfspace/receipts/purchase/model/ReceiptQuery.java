package dev.shelfspace.receipts.purchase.model;

import com.aayushatharva.brotli4j.common.annotations.Local;

import java.time.LocalDate;

/**
 * Defines the filtering, ordering and pagination options used when browsing receipts.
 *
 * <p>Null filter values represent unrestricted criteria. The sort value is supplied by
 * the resouce and defaults to the newest-first ordering</p>
 */
public record ReceiptQuery(
        LocalDate from,
        LocalDate to,
        PurchaseType type,
        ReceiptSort sort,
        int page,
        int size
) {
}
