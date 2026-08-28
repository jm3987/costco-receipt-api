package dev.shelfspace.receipts.purchase.model;

import com.aayushatharva.brotli4j.common.annotations.Local;

import java.time.LocalDate;

/**
 * Defines the filtering and pagination options used when browsing receipts.
 *
 * <p>A null date represents an open boundary. For example, a null {@code from}
 * value includes all receipts up to the optional {@code to} date.</p>
 */
public record ReceiptQuery(
        LocalDate from,
        LocalDate to,
        int page,
        int size
) {
}
