package dev.shelfspace.receipts.repository;


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
import java.util.*;


/**
 * Reads receipt data from the combined Costco CSV export.
 * <p>
 * The export stores one item per row and repeats receipt-level information
 * on every row. This repository groups matching item rows into one
 * ReceiptDetail result.
 */
@ApplicationScoped
public class CsvReceiptRepository implements ReceiptRepository {
    private final Path csvPath;

    @Inject
    public CsvReceiptRepository(@ConfigProperty(name = "receipts.csv.path") String csvPath) {
        this.csvPath = Path.of(csvPath);
    }


    @Override
    public List<ReceiptDetail> findAll(){
        /**
         * LinkedHashMap preserves the order found in the export. Stable ordering
         * makes the API results predictable and simplifies comparisons with source data.
         */
        Map<String, ReceiptAccumulator> receiptsByOrderNumber =
                new LinkedHashMap<>();

        try (
                Reader reader = Files.newBufferedReader(csvPath);
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get()
                        .parse(reader)
        ){
            for (CSVRecord record : parser){
                String orderNumber = record.get("order_number");

                /**
                 * The first row creates the receipt accumulator. Later rows with the
                 * same order number reuse it and contribute additonal items
                 */
                ReceiptAccumulator receipt = receiptsByOrderNumber.computeIfAbsent(orderNumber,
                        ignored -> new ReceiptAccumulator(record)
                );
                receipt.items().add(createItem(record));

            }

            return receiptsByOrderNumber.values()
                    .stream()
                    .map(receipt -> createReceipt(
                            receipt.receiptRecord(),
                            receipt.items()
                    ))
                    .toList();


        } catch (IOException exception){
            throw new UncheckedIOException(
                    "Unable to read receipt CSV: " + csvPath,
                    exception
            );
        }
    }

    @Override
    public Optional<ReceiptDetail> findByOrderNumber(String orderNumber) {
        /**
         * The current export is small enough to parse as a complete collection.
         * Reusing findAll() keeps receipt grouping in one authoritative code path.
         * A future database implementation can provide an indexed lookup without
         * changing this repository contract.
         */
        return findAll()
                .stream()
                .filter(receipt ->
                        orderNumber.equals(receipt.orderNumber())
                )
                .findFirst();
    }

    private ReceiptItem createItem(CSVRecord record) {
        return new ReceiptItem(
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
        );
    }

    private Boolean parseNullableBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Boolean.valueOf(value);
    }

    private ReceiptDetail createReceipt(
            CSVRecord receiptRecord,
            List<ReceiptItem> items
    ) {
        return new ReceiptDetail(
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
        );

    }

    private record ReceiptAccumulator(
            CSVRecord receiptRecord,
            List<ReceiptItem> items
    ){
        private ReceiptAccumulator(CSVRecord receiptRecord){
            this(receiptRecord, new ArrayList<>());
        }
    }

}
