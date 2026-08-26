package dev.shelfspace.receipts.resource;


import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;


@QuarkusTest
public class ReceiptResourceTest {

    @Test
    void returnReceiptWithItems() {
        given()
                .when()
                .get("/api/receipts/ORDER-100")
                .then()
                .statusCode(200)
                .body("orderNumber", is("ORDER-100"))
                .body("receiptId", is("REC-100"))
                .body("receiptType", is("warehouse"))
                .body("transactionDate", is("2025-06-15"))
                .body("warehouseInfo", is("TEST WAREHOUSE"))
                .body("subtotal", is(7.98F))
                .body("taxTotal", is(0.66F))
                .body("finalTotal", is(8.64F))
                .body("items.size()", is(2))
                .body("items.size()", is(2))
                .body("items[0].itemSku", is("SKU-001"))
                .body("items[0].itemName", is("MILK"))
                .body("items[1].itemName", is("BREAD"));
    }

    @Test
    void returnsNotFoundForUnknownReceipt() {
        given()
                .when()
                .get("/api/receipts/DOES-NOT-EXIST")
                .then()
                .statusCode(404);
    }
}
