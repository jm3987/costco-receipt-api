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
}
