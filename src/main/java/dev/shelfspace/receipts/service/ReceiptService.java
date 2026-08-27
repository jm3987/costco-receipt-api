package dev.shelfspace.receipts.service;

import dev.shelfspace.receipts.model.ReceiptDetail;
import dev.shelfspace.receipts.model.ReceiptItem;
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

@ApplicationScoped
public class ReceiptService {

    private final Path csvPath;

    @Inject
    public ReceiptService(
            @ConfigProperty(name = "receipts.csv.path") String csvPath
    ) {
        this.csvPath = Path.of(csvPath);
    }

    public Optional<ReceiptDetail> findByOrderNumber(String orderNumber) {
        List<ReceiptItem> items = new ArrayList<>();

        try (
                Reader reader = Files.newBufferedReader(csvPath);
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get()
                        .parse(reader)
        ) {
            CSVRecord receiptRecord = null;

            for (CSVRecord record : parser) {
                if (!orderNumber.equals(record.get("order_number"))) {
                    continue;
                }

                if (receiptRecord == null) {
                    receiptRecord = record;
                }

                items.add(new ReceiptItem(
                        record.get("item_sku"),
                        record.get("item_name"),
                        record.get("item_actual_name"),
                        record.get("item_description_2"),
                        record.get("item_weight"),
                        record.get("full_item_image"),
                        new BigDecimal(record.get("quantity")),
                        new BigDecimal(record.get("unit_price")),
                        new BigDecimal(record.get("line_total")),
                        record.get("department_id"),
                        "Y".equalsIgnoreCase(record.get("tax_flag")),
                        parseNullableBoolean(record.get("isFSAEligible")),
                        new BigDecimal(record.get("instant_savings")),
                        new BigDecimal(record.get("surcharges")),
                        record.get("surcharge_reason")
                ));
            }

            if (receiptRecord == null) {
                return Optional.empty();
            }

            return Optional.of(new ReceiptDetail(
                    receiptRecord.get("order_number"),
                    receiptRecord.get("receipt_id"),
                    receiptRecord.get("receipt_type"),
                    LocalDate.parse(receiptRecord.get("transaction_date")),
                    receiptRecord.get("warehouse_info"),
                    new BigDecimal(receiptRecord.get("subtotal")),
                    new BigDecimal(receiptRecord.get("discount_amount")),
                    new BigDecimal(receiptRecord.get("shop_card_applied")),
                    new BigDecimal(receiptRecord.get("coupon_applied")),
                    new BigDecimal(receiptRecord.get("tax_total")),
                    new BigDecimal(receiptRecord.get("shipping_handling")),
                    new BigDecimal(receiptRecord.get("delivery_fees")),
                    new BigDecimal(receiptRecord.get("final_total")),
                    List.copyOf(items)
            ));
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to read receipt CSV: " + csvPath,
                    exception
            );
        }
    }

    private Boolean parseNullableBoolean(String value) {
        if (value == null || value.isBlank()){
            return null;
        }
        return Boolean.valueOf(value);
    }
}
