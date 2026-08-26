package dev.shelfspace.receipts;

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

            for (CSVRecord record : parser){
                if (!orderNumber.equals(record.get("order_number"))){
                    continue;
                }

                if (receiptRecord == null){
                    receiptRecord = record;
                }

                items.add(new ReceiptItem(
                        record.get("item_sku"),
                        record.get("item_name"),
                        new BigDecimal(record.get("quantity")),
                        new BigDecimal(record.get("unit_price")),
                        new BigDecimal(record.get("line_total")),
                        new BigDecimal(record.get("instant_savings"))
                ));
            }

            if (receiptRecord == null) {
                return Optional.empty();
            }

            return Optional.of(new ReceiptDetail(
                    receiptRecord.get("order_number"),
                    receiptRecord.get("receipt_id"),
                    receiptRecord.get("receipt_type"),
                    LocalDate.parse(receiptRecord.get("transactionDate")),
                    receiptRecord.get("warehouse_info"),
                    new BigDecimal(receiptRecord.get("subtotal")),
                    new BigDecimal(receiptRecord.get("tax_total")),
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

}
