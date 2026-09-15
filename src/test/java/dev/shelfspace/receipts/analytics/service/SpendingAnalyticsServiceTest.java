package dev.shelfspace.receipts.analytics.service;

import dev.shelfspace.receipts.analytics.model.MonthlySpending;
import dev.shelfspace.receipts.analytics.model.MonthlySpendingByType;
import dev.shelfspace.receipts.analytics.model.SpendingByType;
import dev.shelfspace.receipts.analytics.model.SpendingByTypeSummary;
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

    @Test
    void totalsSpendingByPurchaseTypeAcrossDateRange() {
        ReceiptRepository repository = new StubReceiptRepository(List.of(
                receipt("ORDER-1", "2025-06-01", "warehouse", "10.00"),
                receipt("ORDER-2", "2025-06-15", "gas_station", "20.00"),
                receipt("ORDER-3", "2025-07-20", "warehouse", "30.00"),
                receipt("ORDER-4", "2025-08-01", "online", "99.00")
        ));
        SpendingAnalyticsService service = new SpendingAnalyticsService(repository);
        SpendingAnalyticsQuery query = new SpendingAnalyticsQuery(
                LocalDate.parse("2025-06-01"),
                LocalDate.parse("2025-07-31")
        );

        SpendingByTypeSummary result = service.findSpendingByType(query);

        assertEquals(new SpendingByTypeSummary(
                new BigDecimal("60.00"),
                List.of(
                        new SpendingByType("warehouse", 2, new BigDecimal("40.00")),
                        new SpendingByType("gas_station", 1, new BigDecimal("20.00")),
                        new SpendingByType("online", 0, BigDecimal.ZERO)
                )
        ), result);
    }

    @Test
    void groupsSpendingByMonthAndPurchaseType() {
        ReceiptRepository repository = new StubReceiptRepository(List.of(
                receipt("ORDER-1", "2025-06-01", "warehouse", "10.00"),
                receipt("ORDER-2", "2025-06-15", "gas_station", "20.00"),
                receipt("ORDER-3", "2025-07-20", "warehouse", "30.00"),
                receipt("ORDER-4", "2025-06-25", "warehouse", "5.00")
        ));

        SpendingAnalyticsService service = new SpendingAnalyticsService(repository);
        SpendingAnalyticsQuery query = new SpendingAnalyticsQuery(null, null);

        List<MonthlySpendingByType> result = service.findMonthlySpendingByType(query);
        assertEquals(List.of(
                new MonthlySpendingByType(YearMonth.of(2025, 6), "warehouse", 2, new BigDecimal("15.00")),
                new MonthlySpendingByType(YearMonth.of(2025, 6), "gas_station", 1, new BigDecimal("20.00")),
                new MonthlySpendingByType(YearMonth.of(2025, 7), "warehouse", 1, new BigDecimal("30.00"))
        ), result);
    }

    @Test
    void filtersDateRangeBeforeGroupingSpendingByMonthAndType() {
        ReceiptRepository repository = new StubReceiptRepository(List.of(
                receipt("ORDER-1", "2025-06-01", "warehouse", "10.00"),
                receipt("ORDER-2", "2025-06-15", "gas_station", "20.00"),
                receipt("ORDER-3", "2025-06-30", "warehouse", "30.00")
        ));

        SpendingAnalyticsService service = new SpendingAnalyticsService(repository);
        SpendingAnalyticsQuery query = new SpendingAnalyticsQuery(
                LocalDate.parse("2025-06-10"),
                LocalDate.parse("2025-06-20")
        );

        List<MonthlySpendingByType> result = service.findMonthlySpendingByType(query);
        assertEquals(List.of(
                new MonthlySpendingByType(
                        YearMonth.of(2025, 6),
                        "gas_station",
                        1,
                        new BigDecimal("20.00"
                        ))
        ), result);
    }


    private ReceiptDetail receipt(String orderNumber, String date, String finalTotal) {
        return receipt(orderNumber, date, "warehouse", finalTotal);
    }

    private ReceiptDetail receipt(String orderNumber, String date, String type, String finalTotal) {
        BigDecimal total = new BigDecimal(finalTotal);

        return new ReceiptDetail(
                orderNumber,
                "RECEIPT-" + orderNumber,
                type,
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
