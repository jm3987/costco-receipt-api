package dev.shelfspace.receipts.analytics.service;

import dev.shelfspace.receipts.analytics.model.MonthlySpending;
import dev.shelfspace.receipts.analytics.model.SpendingAnalyticsQuery;
import dev.shelfspace.receipts.purchase.model.ReceiptDetail;
import dev.shelfspace.receipts.purchase.repository.ReceiptRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpendingAnalyticsServiceTest {

    @Test
    void filtersReceiptsBeforeGroupingAPartialMonth() {
        ReceiptRepository repository = new StubReceiptRepository(List.of(
                receipt("ORDER-1", "2025-06-01", "10.00"),
                receipt("ORDER-2", "2025-06-15", "20.00"),
                receipt("ORDER-3", "2025-06-30", "30.00")
        ));
        SpendingAnalyticsService service = new SpendingAnalyticsService(repository);
        SpendingAnalyticsQuery query = new SpendingAnalyticsQuery(
                LocalDate.parse("2025-06-10"),
                LocalDate.parse("2025-06-20")
        );

        List<MonthlySpending> result = service.findMonthlySpending(query);

        assertEquals(
                List.of(new MonthlySpending(
                        YearMonth.of(2025, 6),
                        1,
                        new BigDecimal("20.00")
                )),
                result
        );
    }

    private ReceiptDetail receipt(String orderNumber, String date, String finalTotal) {
        BigDecimal total = new BigDecimal(finalTotal);

        return new ReceiptDetail(
                orderNumber,
                "RECEIPT-" + orderNumber,
                "warehouse",
                LocalDate.parse(date),
                "TEST WAREHOUSE",
                total,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                total,
                List.of()
        );
    }

    private record StubReceiptRepository(
            List<ReceiptDetail> receipts
    ) implements ReceiptRepository {
        @Override
        public List<ReceiptDetail> findAll() {
            return receipts;
        }

        @Override
        public Optional<ReceiptDetail> findByOrderNumber(String orderNumber) {
            return receipts.stream()
                    .filter(receipt -> orderNumber.equals(receipt.orderNumber()))
                    .findFirst();
        }
    }
}
