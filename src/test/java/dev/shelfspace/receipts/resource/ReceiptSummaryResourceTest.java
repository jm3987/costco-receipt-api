package dev.shelfspace.receipts;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


@QuarkusTest
public class ReceiptSummaryResourceTest {

    @Test
    void returnsReceiptSummary(){
        given()
                .when()
                .get("/api/receipts/summary")
                .then()
                .statusCode(200)
                .body("rowCount", is(5))
                .body("receiptCount", is(3))
                .body("firstPurchaseDate", is("2024-01-10"))
                .body("lastPurchaseDate", is("2026-02-20"));
    }
}
