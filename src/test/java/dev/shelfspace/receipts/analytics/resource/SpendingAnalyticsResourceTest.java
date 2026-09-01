package dev.shelfspace.receipts.analytics.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class SpendingAnalyticsResourceTest {

    @Test
    void returnsMonthlySpendingInChronologicalOrder() {
        given()
                .when()
                .get("/api/analytics/spending/monthly")
                .then()
                .statusCode(200)
                .body("size()", is(7))

                // Verify ordering explicitly because clients should not need
                // to sort the data before displaying a chart.
                .body("[0].month", is("2024-01"))
                .body("[0].receiptCount", is(1))
                .body("[0].totalSpending", is(8.24F))

                .body("[1].month", is("2024-03"))
                .body("[1].receiptCount", is(1))
                .body("[1].totalSpending", is(40.00F))

                .body("[2].month", is("2025-01"))
                .body("[2].receiptCount", is(1))
                .body("[2].totalSpending", is(15.00F))

                // Two June receipts verify that monthly analytics aggregate
                // receipt totals rather than returning one row per receipt.
                .body("[3].month", is("2025-06"))
                .body("[3].receiptCount", is(2))
                .body("[3].totalSpending", is(26.00F))

                .body("[4].month", is("2026-02"))
                .body("[4].receiptCount", is(1))
                .body("[4].totalSpending", is(22.00F))

                .body("[5].month", is("2026-04"))
                .body("[5].receiptCount", is(1))
                .body("[5].totalSpending", is(45.00F))

                .body("[6].month", is("2026-07"))
                .body("[6].receiptCount", is(1))
                .body("[6].totalSpending", is(10.32F));
    }
}
