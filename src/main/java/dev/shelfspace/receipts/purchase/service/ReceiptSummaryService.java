package dev.shelfspace.receipts.purchase.service;

import dev.shelfspace.receipts.purchase.model.ReceiptDetail;
import dev.shelfspace.receipts.purchase.model.ReceiptSummary;
import dev.shelfspace.receipts.purchase.repository.ReceiptRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;


/**
 * Calculates aggregate information about the available receipt collection.
 *
 * The service works with the receipt models and remains independent of whether
 * the underlying data comes from a CSV file or a database
 */
@ApplicationScoped
public class ReceiptSummaryService {

    private final ReceiptRepository receiptRepository;

    @Inject
    public ReceiptSummaryService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    public ReceiptSummary getSummary() {
        List<ReceiptDetail> receipts = receiptRepository.findAll();

        /**
         * Each source CSV row represents one receipt item. Summing the grouped
         * item counts therefor preserves the original definition of rowCount.
         */
        int rowCount = receipts.stream()
                .mapToInt(receipt -> receipt.items().size())
                .sum();

        LocalDate firstPurchaseDate = receipts.stream()
                .map(ReceiptDetail::transactionDate)
                .min(LocalDate::compareTo)
                .orElse(null);

        LocalDate lastPurchaseDate = receipts.stream()
                .map(ReceiptDetail::transactionDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        /**
         * ReceiptRepository has already grouped repeated CSV item rows into distinct
         * receipts. Summing finalTotal here therefor counts each receipt exactly once.
         */
        BigDecimal totalSpending = receipts.stream()
                .map(ReceiptDetail::finalTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReceiptSummary(
                rowCount,
                receipts.size(),
                firstPurchaseDate,
                lastPurchaseDate,
                totalSpending
        );
    }
}
