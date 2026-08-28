package dev.shelfspace.receipts.service;

import dev.shelfspace.receipts.model.ReceiptDetail;
import dev.shelfspace.receipts.repository.ReceiptRepository;
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
