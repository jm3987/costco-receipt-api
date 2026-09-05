package dev.shelfspace.receipts.purchase.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ItemResourceTest {

    @Test
    void findsPurchasesByItemNameIgnoringCase() {
        given()
                .queryParam("query", "milk")
                .when()
                .get("/api/items")
                .then()
                .statusCode(200)
                .body("size()", is(4))

                // Item discovery results remain newest first even when the
                // imported source rows are not in chronological order.
                .body("[0].orderNumber", is("ORDER-400"))
                .body("[0].transactionDate", is("2026-07-13"))
                .body("[0].itemSku", is("SKU-001"))
                .body("[0].itemName", is("MILK"))
                .body("[0].unitPrice", is(5.99F))

                .body("[1].orderNumber", is("ORDER-300"))
                .body("[1].unitPrice", is(4.79F))

                .body("[2].orderNumber", is("ORDER-210"))
                .body("[2].unitPrice", is(5.49F))

                .body("[3].orderNumber", is("ORDER-100"))
                .body("[3].unitPrice", is(4.99F));
    }

    @Test
    void findsPurchaseByActualItemName() {
        given()
                .queryParam("query", "wheat")
                .when()
                .get("/api/items")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].itemName", is("BREAD"));
    }

    @Test
    void returnsEmptyListWhenNothingMatches() {
        given()
                .queryParam("query", "NOT-A-REAL-ITEM")
                .when()
                .get("/api/items")
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void rejectsMissingSearchQuery() {
        given()
                .when()
                .get("/api/items")
                .then()
                .statusCode(400);
    }

    @Test
    void returnsPurchaseHistoryForExactSku() {
        given()
                .pathParam("sku", "SKU-001")
                .when()
                .get("/api/items/{sku}/purchases")
                .then()
                .statusCode(200)
                .body("size()", is(4))
                .body("itemSku", everyItem(is("SKU-001")))

                // Exact-SKU history must be returned newest first.
                .body("[0].orderNumber", is("ORDER-400"))
                .body("[1].orderNumber", is("ORDER-300"))
                .body("[2].orderNumber", is("ORDER-210"))
                .body("[3].orderNumber", is("ORDER-100"));
    }

    @Test
    void matchesExactSkuWhenDescriptionsDiffer() {
        given()
                .pathParam("sku", "SKU-001")
                .when()
                .get("/api/items/{sku}/purchases")
                .then()
                .statusCode(200)
                .body("size()", is(4))
                .body(
                        "itemActualName",
                        hasItems(
                                "FRESH WHOLE MILK",
                                "VITAMIN D MILK",
                                "WHOLE MILK"
                        )
                )
                .body("itemSku", everyItem(is("SKU-001")));
    }

    @Test
    void returnsNotFoundForPartialSku() {
        given()
                .pathParam("sku", "SKU-00")
                .when()
                .get("/api/items/{sku}/purchases")
                .then()
                .statusCode(404);
    }

    @Test
    void returnsNotFoundForUnknownSku() {
        given()
                .pathParam("sku", "SKU-999")
                .when()
                .get("/api/items/{sku}/purchases")
                .then()
                .statusCode(404);
    }

    @Test
    void returnsStatisticsForExactSku() {
        given()
                .pathParam("sku", "SKU-001")
                .when()
                .get("/api/items/{sku}/statistics")
                .then()
                .statusCode(200)
                .body("sku", is("SKU-001"))
                .body("displayName", is("FRESH WHOLE MILK"))
                .body("receiptCount", is(4))
                .body("firstPurchaseDate", is("2024-01-10"))
                .body("lastPurchaseDate", is("2026-07-13"))
                .body("lowestPrice", is(4.79F))
                .body("highestPrice", is(5.99F))
                // The unrounded mean is 5.315, proving the selected
                // HALF_UP currency rule produces 5.32.
                .body("averagePrice", is(5.32F))
                .body("latestPrice", is(5.99F));
    }

    @Test
    void returnsStatisticsForSinglePurchaseSku(){
        given()
                .pathParam("sku", "SKU-005")
                .when()
                .get("/api/items/{sku}/statistics")
                .then()
                .statusCode(200)
                .body("sku", is("SKU-005"))
                .body("displayName", is("LARGE EGGS"))
                .body("receiptCount", is(1))
                .body("firstPurchaseDate", is("2025-06-15"))
                .body("lastPurchaseDate", is("2025-06-15"))
                .body("lowestPrice", is(6.00F))
                .body("highestPrice", is(6.00F))
                .body("averagePrice", is(6.00F))
                .body("latestPrice", is(6.00F));
    }

    @Test
    void returnsNotFoundForUnknownSkuStatistics() {
        given()
                .pathParam("sku", "SKU-999")
                .when()
                .get("/api/items/{sku}/statistics")
                .then()
                .statusCode(404);
    }

    @Test
    void returnsStatisticsAndPurchaseFrequencyForRecurringSku() {
        given()
                .when()
                .get("/api/items/SKU-001/statistics")
                .then()
                .statusCode(200)
                .body("sku", is("SKU-001"))
                .body("displayName", is("FRESH WHOLE MILK"))
                .body("receiptCount", is(4))
                .body("firstPurchaseDate", is("2024-01-10"))
                .body("lastPurchaseDate", is("2026-07-13"))
                .body("lowestPrice", is(4.79F))
                .body("highestPrice", is(5.99F))
                .body("averagePrice", is(5.32F))
                .body("latestPrice", is(5.99F))
                .body("averageDaysBetweenPurchases", is(305))
                .body("purchaseIntervalCount", is(3));
    }

    @Test
    void returnsNoAverageIntervalForSkuPurchasedOnce() {
        given()
                .when()
                .get("/api/items/SKU-005/statistics")
                .then()
                .statusCode(200)
                .body("receiptCount", is(1))
                .body("averageDaysBetweenPurchases", nullValue())
                .body("purchaseIntervalCount", is(0));
    }

    @Test
    void returnsNotFoundWhenStatisticsSkuDoesNotExist() {
        given()
                .when()
                .get("/api/items/SKU-999/statistics")
                .then()
                .statusCode(404);
    }
}
