package dev.shelfspace.receipts.purchase.model;


import java.util.List;

/**
 * Paged collection of receipt overviews with the metadata needed to
 * request additional pages.
 */
public record ReceiptPage(
        List<ReceiptOverview> receipts,
        int page,
        int size,
        long totalReceipts,
        int totalPages
) {
}
