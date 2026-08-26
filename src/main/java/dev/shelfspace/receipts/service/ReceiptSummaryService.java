package dev.shelfspace.receipts;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class ReceiptSummaryService {

    private final Path csvPath;

    @Inject
    public ReceiptSummaryService(
            @ConfigProperty(name = "receipts.csv.path") String csvPath){
        this.csvPath = Path.of(csvPath);
    }
    public ReceiptSummary getSummary(
    ) {
        int rowCount = 0;
        Set<String> orderNumbers = new HashSet<>();
        LocalDate firstPurchaseDate = null;
        LocalDate lastPurchaseDate = null;

        try(
                Reader reader = Files.newBufferedReader(csvPath);
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get()
                        .parse(reader)

                ) {
            //System.out.println(parser.getHeaderNames());
            for (CSVRecord record : parser) {
                rowCount++;
                orderNumbers.add(record.get("order_number"));

                LocalDate purchaseDate = LocalDate.parse(
                        record.get("transaction_date")
                );

                if (firstPurchaseDate == null ||
                        purchaseDate.isBefore(firstPurchaseDate)){
                    firstPurchaseDate = purchaseDate;
                }
                if (lastPurchaseDate == null ||
                    purchaseDate.isAfter(lastPurchaseDate)){
                    lastPurchaseDate = purchaseDate;
                }


//                if (rowCount <= 3){
//                    System.out.println(record.toMap());
//                }

//                if (rowCount <= 3) {
//                    Log.infof(
//                            "Row %d: order=%s, date=%s, item=%s",
//                            rowCount,
//                            record.get("order_number"),
//                            record.get("transaction_date"),
//                            record.get("item_name")
//                    );
//                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to read receipt CSV: " + csvPath,
                    exception
            );
        }

        return new ReceiptSummary(
                rowCount,
                orderNumbers.size(),
                firstPurchaseDate,
                lastPurchaseDate
        );
    }
}
