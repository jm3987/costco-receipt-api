package dev.shelfspace.receipts.analytics.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
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

    @Test
    void filtersMonthlySpendingUsingInclusiveDateRange() {
        given()
                .queryParam("from", "2025-01-05")
                .queryParam("to", "2025-06-15")
                .when()
                .get("/api/analytics/spending/monthly")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].month", is("2025-01"))
                .body("[0].receiptCount", is(1))
                .body("[0].totalSpending", is(15.00F))
                .body("[1].month", is("2025-06"))
                .body("[1].receiptCount", is(2))
                .body("[1].totalSpending", is(26.00F));
    }

    @Test
    void filtersMonthlySpendingFromDateWithoutUpperBoundary() {
        given()
                .queryParam("from", "2026-02-20")
                .when()
                .get("/api/analytics/spending/monthly")
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("[0].month", is("2026-02"))
                .body("[0].totalSpending", is(22.00F))
                .body("[1].month", is("2026-04"))
                .body("[2].month", is("2026-07"));
    }

    @Test
    void filtersMonthlySpendingToDateWithoutLowerBoundary() {
        given()
                .queryParam("to", "2025-01-05")
                .when()
                .get("/api/analytics/spending/monthly")
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("[0].month", is("2024-01"))
                .body("[1].month", is("2024-03"))
                .body("[2].month", is("2025-01"))
                .body("[2].totalSpending", is(15.00F));
    }

    @Test
    void returnsEmptyMonthlySpendingWhenDateRangeHasNoReceipts() {
        given()
                .queryParam("from", "2030-01-01")
                .when()
                .get("/api/analytics/spending/monthly")
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void rejectsMonthlySpendingDateRangeWhenFromIsAfterTo() {
        given()
                .queryParam("from", "2026-01-01")
                .queryParam("to", "2025-01-01")
                .when()
                .get("/api/analytics/spending/monthly")
                .then()
                .statusCode(400)
                .body("code", is("INVALID_SPENDING_ANALYTICS_QUERY"))
                .body(
                        "message",
                        is("Query parameter 'from' must not be after 'to'.")
                );
    }

    @Test
    void returnsRangeWideSpendingByTypeAndGrandTotal() {
        given()
                .queryParam("from", "2025-01-05")
                .queryParam("to", "2025-06-15")
                .when()
                .get("/api/analytics/spending/by-type")
                .then()
                .statusCode(200)
                .body("grandTotalSpending", is(41.00F))
                .body("breakdown.size()", is(3))
                .body("breakdown[0].purchaseType", is("warehouse"))
                .body("breakdown[0].receiptCount", is(2))
                .body("breakdown[0].totalSpending", is(26.00F))
                .body("breakdown[1].purchaseType", is("gas_station"))
                .body("breakdown[1].receiptCount", is(0))
                .body("breakdown[1].totalSpending", is(0))
                .body("breakdown[2].purchaseType", is("online"))
                .body("breakdown[2].receiptCount", is(1))
                .body("breakdown[2].totalSpending", is(15.00F));
    }

    @Test
    void includesAllThreePurchaseTypesInGrandTotal() {
        given()
                .when()
                .get("/api/analytics/spending/by-type")
                .then()
                .statusCode(200)
                .body("grandTotalSpending", is(166.56F))
                .body("breakdown.size()", is(3))
                .body("breakdown[0].purchaseType", is("warehouse"))
                .body("breakdown[0].totalSpending", is(44.56F))
                .body("breakdown[1].purchaseType", is("gas_station"))
                .body("breakdown[1].totalSpending", is(85.00F))
                .body("breakdown[2].purchaseType", is("online"))
                .body("breakdown[2].totalSpending", is(37.00F));
    }

    @Test
    void returnsZeroSpendingByTypeWhenDateRangeHasNoReceipts() {
        given()
                .queryParam("from", "2030-01-01")
                .when()
                .get("/api/analytics/spending/by-type")
                .then()
                .statusCode(200)
                .body("grandTotalSpending", is(0))
                .body("breakdown.size()", is(3))
                .body("breakdown[0].receiptCount", is(0))
                .body("breakdown[1].receiptCount", is(0))
                .body("breakdown[2].receiptCount", is(0));
    }

    @Test
    void rejectsSpendingByTypeWhenFromIsAfterTo() {
        given()
                .queryParam("from", "2026-01-01")
                .queryParam("to", "2025-01-01")
                .when()
                .get("/api/analytics/spending/by-type")
                .then()
                .statusCode(400)
                .body("code", is("INVALID_SPENDING_ANALYTICS_QUERY"))
                .body("message", is("Query parameter 'from' must not be after 'to'."));
    }

    @Test
    void rejectsMalformedSpendingByTypeDate() {
        given()
                .queryParam("to", "not-a-date")
                .when()
                .get("/api/analytics/spending/by-type")
                .then()
                .statusCode(400)
                .body("code", is("INVALID_DATE_PARAMETER"))
                .body("message", containsString("Query parameter 'to'"))
                .body("message", containsString("YYYY-MM-DD"));
    }

    @Test
    void rejectsMalformedMonthlySpendingDate() {
        given()
                .queryParam("to", "not-a-date")
                .when()
                .get("/api/analytics/spending/monthly")
                .then()
                .statusCode(400)
                .body("code", is("INVALID_DATE_PARAMETER"))
                .body("message", containsString("Query parameter 'to'"))
                .body("message", containsString("YYYY-MM-DD"));
    }

    @Test
    void returnsMonthlySpendingByTypeWithinInclusiveDateRange() {
        given()
                .queryParam("from", "2025-01-05")
                .queryParam("to", "2025-06-15")
                .when()
                .get("/api/analytics/spending/monthly/by-type")
                .then()
                .statusCode(200)
                .body("size()", is(2))

                .body("[0].month", is("2025-01"))
                .body("[0].purchaseType", is("online"))
                .body("[0].receiptCount", is(1))
                .body("[0].totalSpending", is(15.00F))

                .body("[1].month", is("2025-06"))
                .body("[1].purchaseType", is("warehouse"))
                .body("[1].receiptCount", is(2))
                .body("[1].totalSpending", is(26.00F));
    }

    @Test
    void returnsEmptyMonthlySpendingByTypeWhenDateRangeHasNoReceipts() {
        given()
                .queryParam("from", "2030-01-01")
                .when()
                .get("/api/analytics/spending/monthly/by-type")
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void rejectsMonthlySpendingByTypeWhenFromIsAfterTo() {
        given()
                .queryParam("from", "2026-01-01")
                .queryParam("to", "2025-01-01")
                .when()
                .get("/api/analytics/spending/monthly/by-type")
                .then()
                .statusCode(400)
                .body("code", is("INVALID_SPENDING_ANALYTICS_QUERY"))
                .body(
                        "message",
                        is("Query parameter 'from' must not be after 'to'.")
                );
    }

    @Test
    void rejectsMalformedMonthlySpendingByTypeDate() {
        given()
                .queryParam("to", "not-a-date")
                .when()
                .get("/api/analytics/spending/monthly/by-type")
                .then()
                .statusCode(400)
                .body("code", is("INVALID_DATE_PARAMETER"))
                .body("message", containsString("Query parameter 'to'"))
                .body("message", containsString("YYYY-MM-DD"));
    }
}
