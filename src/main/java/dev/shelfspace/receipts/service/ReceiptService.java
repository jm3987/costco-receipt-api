package dev.shelfspace.receipts.service;

import dev.shelfspace.receipts.model.ReceiptDetail;
import dev.shelfspace.receipts.model.ReceiptItem;
import dev.shelfspace.receipts.repository.ReceiptRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
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
}
