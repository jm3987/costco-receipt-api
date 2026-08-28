package dev.shelfspace.receipts.purchase.service;

import dev.shelfspace.receipts.purchase.model.ReceiptDetail;
import dev.shelfspace.receipts.purchase.model.ReceiptOverview;
import dev.shelfspace.receipts.purchase.model.ReceiptPage;
import dev.shelfspace.receipts.purchase.model.ReceiptQuery;
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

    /**
     * Returns one page of compact receipt overviews in newest-first order.
     * This method currently assumes that page is non-negative and size
     * is positive. Complete pagination validation will be added in future
     * @param page
     * @param size
     * @return ReceiptPage
     */
    public ReceiptPage listReceipts(int page, int size){
        List<ReceiptOverview> receiptOverviews =
                receiptRepository.findAll()
                        .stream()
                        //Newsest-first is the default browsing order.
                        //Order number provides deterministic ordering for same-date receipts.
                        .sorted(
                                Comparator.comparing(ReceiptDetail::transactionDate)
                                        .reversed()
                                        .thenComparing(ReceiptDetail::orderNumber)
                        )
                        .map(this::toReceiptOverview)
                        .toList();

        long totalReceipts = receiptOverviews.size();
        int totalPages = (int) Math.ceil((double) totalReceipts / size );

        //Page numbers are zero-based: page 0 skips nothing, while page 1 skips the first complete page.
        long offset = (long) page * size;

        List<ReceiptOverview> pageReceipts =
                receiptOverviews.stream()
                        .skip(offset)
                        .limit(size)
                        .toList();

        return new ReceiptPage(
                pageReceipts,
                page,
                size,
                totalReceipts,
                totalPages
        );
    }

    public ReceiptPage listReceipts(ReceiptQuery query){
        validateDateRange(query.from(), query.to());

        List<ReceiptOverview> receiptOverviews = receiptRepository.findAll().stream()
                //Filtering must happen before pagination so page totals describe
                // the complete filtered result rather than only the current page
                .filter(receipt -> isWithinDateRange(
                        receipt.transactionDate(),
                        query.from(),
                        query.to()
                ))
                .sorted(
                        Comparator.comparing(ReceiptDetail::transactionDate)
                                .reversed()
                                .thenComparing(ReceiptDetail::orderNumber)
                )
                .map(this::toReceiptOverview)
                .toList();

        long totalReceipts = receiptOverviews.size();
        int totalPages = (int) Math.ceil((double) totalReceipts / query.size());
        long offset = (long) query.page() * query.size();

        List<ReceiptOverview> pageReceipts = receiptOverviews.stream()
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
     * Determines whether a recipt date falls within the request inclusive range.
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
}
