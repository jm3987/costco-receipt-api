package dev.shelfspace.receipts.purchase.service;

import dev.shelfspace.receipts.purchase.model.*;
import dev.shelfspace.receipts.purchase.repository.ReceiptRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * Coordinates receipt-related application operations.
 * <p>
 * HTTP behavior belongs to the resource layer, while storage and CSV
 * conversion belong to the repository layer.
 */
@ApplicationScoped
public class ReceiptService {
    private final ReceiptRepository receiptRepository;

    @Inject
    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    public Optional<ReceiptDetail> findByOrderNumber(String orderNumber) {

        return receiptRepository.findByOrderNumber(orderNumber);
    }

    public ReceiptPage listReceipts(ReceiptQuery query) {
        validatePagination(query.page(), query.size());
        validateDateRange(query.from(), query.to());

        List<ReceiptOverview> receiptOverviews =
                receiptRepository.findAll().stream()
                    // Filters must run before sorting and pagination so the
                    // page metadata represents the filtered result.
                    .filter(receipt -> isWithinDateRange(
                            receipt.transactionDate(),
                            query.from(),
                            query.to()
                    ))
                    .filter(receipt -> matchesPurchaseType(
                            receipt.receiptType(),
                            query.type()
                    ))
                    .sorted(receiptComparator(query.sort()))
                    .map(this::toReceiptOverview)
                    .toList();

        long totalReceipts = receiptOverviews.size();
        int totalPages = (int) Math.ceil((double) totalReceipts / query.size());
        //Perform the multiplication as a long so a large page number cannot overflow Java's smaller int range before skip() receives the offset.
        long offset = (long) query.page() * query.size();

        List<ReceiptOverview> pageReceipts =
                receiptOverviews.stream()
                    .skip(offset)
                    .limit(query.size())
                    .toList();

        return new ReceiptPage(
                pageReceipts,
                query.page(),
                query.size(),
                totalReceipts,
                totalPages
        );
    }

    private ReceiptOverview toReceiptOverview(ReceiptDetail receipt) {
        return new ReceiptOverview(
                receipt.orderNumber(),
                receipt.transactionDate(),
                receipt.receiptType(),
                receipt.warehouseInfo(),
                receipt.finalTotal()
        );
    }

    /**
     * Determines whether a receipt date falls within the request inclusive range.
     */
    private boolean isWithinDateRange(LocalDate transactionDate, LocalDate from, LocalDate to) {
        boolean isOnOrAfterFrom = from == null || !transactionDate.isBefore(from);
        boolean isOnOrBeforeTo = to == null || !transactionDate.isAfter(to);
        return isOnOrAfterFrom && isOnOrBeforeTo;
    }

    /**
     * Rejects a range whose lower boundary occurs after its upper boundary.
     */
    private void validateDateRange(LocalDate from, LocalDate to) {
        if(from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Query parameter 'from' must not be after 'to'.");
        }
    }

    /**
     * Determines whether a receipt belongs to the requested purchase type.
     *
     * <p>A null requested type represents an unrestricted search. Receipt values
     * are compared case-insensitively because imported text should not make the
     * public filter unexpectedly case-sensitive.</p>
     */
    private boolean matchesPurchaseType(
            String receiptType,
            PurchaseType requestedType
    ) {
        if (requestedType == null) {
            return true;
        }

        return requestedType.value().equalsIgnoreCase(receiptType);
    }

    /**
     * Creates the requested date comparator with a deterministic secondary order.
     *
     * Order number remains ascending in both modes. This prevents receipts
     * sharing the same date from moving unpredictably between pages.
     */
    private Comparator<ReceiptDetail> receiptComparator(ReceiptSort sort){
        Comparator<ReceiptDetail> dateComparator = Comparator.comparing((ReceiptDetail::transactionDate));

        if (sort == ReceiptSort.DATE_DESC) {
            dateComparator = dateComparator.reversed();
        }

        return dateComparator.thenComparing(ReceiptDetail::orderNumber);
    }

    /**
     * Validates the paging limits used when browsing receipts.
     *
     * <p>The maximum page size protects the application from requests that would
     * otherwise return an unnecessarily large response.</p>
     */
    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Query parameter 'page' must be zero or greater."
            );
        }

        if (size < 1) {
            throw new IllegalArgumentException(
                    "Query parameter 'size' must be at least 1."
            );
        }

        if (size > 100) {
            throw new IllegalArgumentException(
                    "Query parameter 'size' must not exceed 100."
            );
        }
    }
}
