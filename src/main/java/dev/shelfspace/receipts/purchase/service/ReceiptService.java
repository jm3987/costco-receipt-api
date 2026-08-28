package dev.shelfspace.receipts.purchase.service;

import dev.shelfspace.receipts.purchase.model.ReceiptDetail;
import dev.shelfspace.receipts.purchase.repository.ReceiptRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
}
