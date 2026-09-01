package dev.shelfspace.receipts.purchase.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;


@QuarkusTest
public class ReceiptSummaryResourceTest {

    @Test
    void returnsReceiptSummary() {
        given()
                .when()
                .get("/api/receipts/summary")
                .then()
                .statusCode(200)
                .body("rowCount", is(13))
                .body("receiptCount", is(8))
                .body("firstPurchaseDate", is("2024-01-10"))
                .body("lastPurchaseDate", is("2026-07-13"))
                .body("totalSpending", is(166.56F));
    }
}
