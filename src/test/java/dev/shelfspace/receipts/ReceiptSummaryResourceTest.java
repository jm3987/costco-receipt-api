package dev.shelfspace.receipts;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class ReceiptSummaryResourceTest {

    @Test
    void returnsReceiptSummary(){
        given()
                .when()
                .get("/api/receipts/summary")
                .then()
                .statusCode(200)
                .body("rowCount", equalTo(1744))
                .body("receiptCount", equalTo(213))
                .body("firstPurchaseDate", equalTo("2024-01-11"))
                .body("lastPurchaseDate", equalTo("2026-07-13"));
    }
}
