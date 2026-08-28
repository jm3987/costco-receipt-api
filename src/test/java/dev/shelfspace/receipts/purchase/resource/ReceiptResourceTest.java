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
                .body("totalReceipts", is(3))
                .body("totalPages", is(1))
                .body("receipts.size()", is(3))

                // The API contract requires newest-first ordering.
                .body("receipts[0].orderNumber", is("ORDER-300"))
                .body("receipts[0].transactionDate", is("2026-02-20"))
                .body("receipts[0].finalTotal", is(16.24F))

                .body("receipts[1].orderNumber", is("ORDER-100"))
                .body("receipts[2].orderNumber", is("ORDER-200"))

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
                .body("totalReceipts", is(3))
                .body("totalPages", is(2))
                .body("receipts.size()", is(1))
                // With newest-first ordering, ORDER-200 is the third receipt
                // and therefore the only receipt on zero-based page 1.
                .body("receipts[0].orderNumber", is("ORDER-200"));

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
                .body("receipts", hasSize(2))
                // The normal newest-first ordering is retained after filtering.
                .body("receipts[0].orderNumber", equalTo("ORDER-300"))
                .body("receipts[1].orderNumber", equalTo("ORDER-100"))
                .body("totalReceipts", equalTo(2))
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
                .body("receipts", hasSize(2))
                .body("receipts[0].orderNumber", equalTo("ORDER-300"))
                .body("receipts[1].orderNumber", equalTo("ORDER-100"))
                .body("totalReceipts", equalTo(2));
    }

    @Test
    void shouldFilterReceiptsToDateWithoutLowerBoundary() {
        given()
                .queryParam("to", "2025-06-15")
                .when()
                .get("/api/receipts")
                .then()
                .statusCode(200)
                .body("receipts", hasSize(2))
                .body("receipts[0].orderNumber", equalTo("ORDER-100"))
                .body("receipts[1].orderNumber", equalTo("ORDER-200"))
                .body("totalReceipts", equalTo(2));
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
}
