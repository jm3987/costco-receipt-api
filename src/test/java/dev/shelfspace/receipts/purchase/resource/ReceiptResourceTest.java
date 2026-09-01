package dev.shelfspace.receipts.purchase.resource;


import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@QuarkusTest
public class ReceiptResourceTest {

    @Test
    void returnReceiptWithItems() {
        given()
                .when()
                .get("/api/receipts/ORDER-100")
                .then()
                .statusCode(200)
                .body("discountAmount", is(0))
                .body("shippingHandling", is(0))
                .body("deliveryFees", is(0))
                .body("items[0].itemActualName", is("WHOLE MILK"))
                .body("items[0].description", is("1 GALLON"))
                .body("items[0].imageUrl", is("https://example.test/milk.jpg"))
                .body("items[0].departmentId", is("12"))
                .body("items[0].taxable", is(false))
                .body("items[0].fsaEligible", is(true))
                .body("items[1].taxable", is(true))
                .body("items[1].fsaEligible", is(false))
                .body("items[1].instantSavings", is(1.00F));
    }

    @Test
    void returnsNotFoundForUnknownReceipt() {
        given()
                .when()
                .get("/api/receipts/DOES-NOT-EXIST")
                .then()
                .statusCode(404);
    }

    @Test
    void listReceiptOverviewsUsingDefaultPagination() {
        given()
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("page", is(0))
                .body("size", is(20))
                .body("totalReceipts", is(8))
                .body("totalPages", is(1))
                .body("receipts.size()", is(8))

                // The API contract requires newest-first ordering.
                .body("receipts[0].orderNumber", is("ORDER-400"))
                .body("receipts[0].transactionDate", is("2026-07-13"))
                .body("receipts[0].finalTotal", is(10.32F))

                .body("receipts[1].orderNumber", is("ORDER-310"))
                .body("receipts[2].orderNumber", is("ORDER-300"))
                .body("receipts[3].orderNumber", is("ORDER-210"))
                .body("receipts[4].orderNumber", is("ORDER-220"))
                .body("receipts[5].orderNumber", is("ORDER-200"))
                .body("receipts[6].orderNumber", is("ORDER-110"))
                .body("receipts[7].orderNumber", is("ORDER-100"))

                // Verify that the compact response includes the selected fields,
                // even when warehouse information is not applicable.
                .body("receipts[0]", hasKey("receiptType"))
                .body("receipts[0]", hasKey("warehouseInfo"));
    }

    @Test
    void returnsTheRequestedReceiptPage() {
        given()
                .queryParam("page", 1)
                .queryParam("size", 2)
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("size", is(2))
                .body("totalReceipts", is(8))
                .body("totalPages", is(4))
                .body("receipts.size()", is(2))
                // Page 0 contains ORDER-400 and ORDER-310. The next two
                // newest receipts therefore appear on zero-based page 1.
                .body("receipts[0].orderNumber", is("ORDER-300"))
                .body("receipts[1].orderNumber", is("ORDER-210"));

    }

    @Test
    void shouldFilterReceiptsUsingInclusiveDateRange() {
        given()
                .queryParam("from", "2025-06-15")
                .queryParam("to", "2026-02-20")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", hasSize(3))
                // The normal newest-first ordering is retained after filtering.
                .body("receipts[0].orderNumber", equalTo("ORDER-300"))
                .body("receipts[1].orderNumber", equalTo("ORDER-210"))
                .body("receipts[2].orderNumber", equalTo("ORDER-220"))
                .body("totalReceipts", equalTo(3))
                .body("totalPages", equalTo(1));
    }

    @Test
    void shouldFilterReceiptsFromDateWithoutUpperBoundary() {
        given()
                .queryParam("from", "2025-06-15")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", hasSize(5))
                .body("receipts[0].orderNumber", equalTo("ORDER-400"))
                .body("receipts[1].orderNumber", equalTo("ORDER-310"))
                .body("receipts[2].orderNumber", equalTo("ORDER-300"))
                .body("receipts[3].orderNumber", equalTo("ORDER-210"))
                .body("receipts[4].orderNumber", equalTo("ORDER-220"))
                .body("totalReceipts", equalTo(5));
    }

    @Test
    void shouldFilterReceiptsToDateWithoutLowerBoundary() {
        given()
                .queryParam("to", "2025-06-15")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", hasSize(5))
                .body("receipts[0].orderNumber", equalTo("ORDER-210"))
                .body("receipts[1].orderNumber", equalTo("ORDER-220"))
                .body("receipts[2].orderNumber", equalTo("ORDER-200"))
                .body("receipts[3].orderNumber", equalTo("ORDER-110"))
                .body("receipts[4].orderNumber", equalTo("ORDER-100"))
                .body("totalReceipts", equalTo(5));
    }

    @Test
    void shouldReturnEmptyPageWhenDateRangeHasNoReceipts() {
        given()
                .queryParam("from", "2030-01-01")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", empty())
                .body("page", equalTo(0))
                .body("size", equalTo(20))
                .body("totalReceipts", equalTo(0))
                .body("totalPages", equalTo(0));
    }

    @Test
    void shouldRejectDateRangeWhenFromIsAfterTo() {
        given()
                .queryParam("from", "2026-01-01")
                .queryParam("to", "2025-01-01")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldRejectInvalidDateFormat() {
        given()
                .queryParam("from", "not-a-date")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldFilterReceiptsByPurchaseType() {
        given()
                .queryParam("type", "warehouse")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", hasSize(4))
                .body("receipts[0].orderNumber", equalTo("ORDER-400"))
                .body("receipts[1].orderNumber", equalTo("ORDER-210"))
                .body("receipts[2].orderNumber", equalTo("ORDER-220"))
                .body("receipts[3].orderNumber", equalTo("ORDER-100"))
                .body("receipts[0].receiptType", equalTo("warehouse"))
                .body("totalReceipts", equalTo(4))
                .body("totalPages", equalTo(1));
    }

    @Test
    void shouldMatchPurchaseTypeCaseInsensitively() {
        given()
                .queryParam("type", "ONLINE")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", hasSize(2))
                .body("receipts[0].orderNumber", equalTo("ORDER-300"))
                .body("receipts[1].orderNumber", equalTo("ORDER-200"))
                .body("receipts[0].receiptType", equalTo("online"))
                .body("totalReceipts", equalTo(2));
    }

    @Test
    void shouldFilterGasStationReceipts() {
        given()
                .queryParam("type", "gas_station")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", hasSize(2))
                .body("receipts[0].orderNumber", equalTo("ORDER-310"))
                .body("receipts[1].orderNumber", equalTo("ORDER-110"))
                .body("receipts[0].receiptType", equalTo("gas_station"))
                .body("totalReceipts", equalTo(2));
    }

    @Test
    void shouldCombinePurchaseTypeAndDateFilters() {
        given()
                .queryParam("type", "warehouse")
                .queryParam("from", "2026-01-01")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                // Several receipts satisfy the date filter, but only
                // ORDER-400 is also a warehouse receipt.
                .body("receipts", hasSize(1))
                .body("receipts[0].orderNumber", equalTo("ORDER-400"))
                .body("totalReceipts", equalTo(1))
                .body("totalPages", equalTo(1));
    }

    @Test
    void shouldRejectUnsupportedPurchaseType() {
        given()
                .queryParam("type", "delivery")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(400)
                .body(
                        "code",
                        equalTo("INVALID_PURCHASE_TYPE")
                )
                .body(
                        "message",
                        containsString(
                                "Supported values: warehouse, gas_station, online"
                        )
                );
    }

    @Test
    void shouldSortReceiptsByDateAscending() {
        given()
                .queryParam("sort", "date,asc")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", hasSize(8))
                .body("receipts[0].orderNumber", equalTo("ORDER-100"))
                .body("receipts[1].orderNumber", equalTo("ORDER-110"))
                .body("receipts[2].orderNumber", equalTo("ORDER-200"))
                // The two receipts on 2025-06-15 verify the deterministic
                // ascending order-number tie-breaker.
                .body("receipts[3].orderNumber", equalTo("ORDER-210"))
                .body("receipts[4].orderNumber", equalTo("ORDER-220"))
                .body("receipts[5].orderNumber", equalTo("ORDER-300"))
                .body("receipts[6].orderNumber", equalTo("ORDER-310"))
                .body("receipts[7].orderNumber", equalTo("ORDER-400"));
    }

    @Test
    void shouldSortReceiptsByDateDescending() {
        given()
                .queryParam("sort", "date,desc")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", hasSize(8))
                .body("receipts[0].orderNumber", equalTo("ORDER-400"))
                .body("receipts[1].orderNumber", equalTo("ORDER-310"))
                .body("receipts[2].orderNumber", equalTo("ORDER-300"))
                .body("receipts[3].orderNumber", equalTo("ORDER-210"))
                .body("receipts[4].orderNumber", equalTo("ORDER-220"))
                .body("receipts[5].orderNumber", equalTo("ORDER-200"))
                .body("receipts[6].orderNumber", equalTo("ORDER-110"))
                .body("receipts[7].orderNumber", equalTo("ORDER-100"));
    }

    @Test
    void shouldMatchReceiptSortCaseInsensitively() {
        given()
                .queryParam("sort", "DATE,ASC")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts[0].orderNumber", equalTo("ORDER-100"))
                .body("receipts[7].orderNumber", equalTo("ORDER-400"));
    }

    @Test
    void shouldRejectUnsupportedReceiptSortField() {
        given()
                .queryParam("sort", "total,asc")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_RECEIPT_SORT"))
                .body(
                        "message",
                        containsString("Supported values")
                );
    }

    @Test
    void shouldRejectUnsupportedReceiptSortDirection() {
        given()
                .queryParam("sort", "date,sideways")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_RECEIPT_SORT"))
                .body(
                        "message",
                        containsString("date,desc")
                )
                .body(
                        "message",
                        containsString("date,asc")
                );
    }

    @Test
    void shouldFilterBeforeSortingAndPagination() {
        given()
                .queryParam("from", "2025-01-01")
                .queryParam("sort", "date,asc")
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                // Six receipts remain after filtering. They are sorted before
                // the first one-receipt page is selected.
                .body("receipts", hasSize(1))
                .body("receipts[0].orderNumber", equalTo("ORDER-200"))
                .body("totalReceipts", equalTo(6))
                .body("totalPages", equalTo(6));
    }

    @Test
    void shouldRejectNegativePageNumber() {
        given()
                .queryParam("page", -1)
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(400)
                .body(
                        "code",
                        equalTo("INVALID_RECEIPT_QUERY")
                )
                .body(
                        "message",
                        containsString(
                                "'page' must be zero or greater"
                        )
                );
    }

    @Test
    void shouldRejectPageSizeBelowMinimum() {
        given()
                .queryParam("size", 0)
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(400)
                .body(
                        "code",
                        equalTo("INVALID_RECEIPT_QUERY")
                )
                .body(
                        "message",
                        containsString(
                                "'size' must be at least 1"
                        )
                );
    }

    @Test
    void shouldRejectPageSizeAboveMaximum() {
        given()
                .queryParam("size", 101)
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(400)
                .body(
                        "code",
                        equalTo("INVALID_RECEIPT_QUERY")
                )
                .body(
                        "message",
                        containsString(
                                "'size' must not exceed 100"
                        )
                );
    }

    @Test
    void shouldAcceptMaximumPageSize() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 100)
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("page", equalTo(0))
                .body("size", equalTo(100))
                .body("totalReceipts", equalTo(8))
                .body("totalPages", equalTo(1));
    }

    @Test
    void shouldReturnEmptyPageWhenPageIsBeyondAvailableResults() {
        given()
                .queryParam("page", 4)
                .queryParam("size", 2)
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", empty())
                .body("page", equalTo(4))
                .body("size", equalTo(2))
                .body("totalReceipts", equalTo(8))
                .body("totalPages", equalTo(4));
    }

    @Test
    void shouldCalculatePaginationMetadataAfterFiltering() {
        given()
                .queryParam("from", "2025-01-01")
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", hasSize(1))
                .body("page", equalTo(0))
                .body("size", equalTo(1))
                .body("totalReceipts", equalTo(6))
                .body("totalPages", equalTo(6));
    }
}
