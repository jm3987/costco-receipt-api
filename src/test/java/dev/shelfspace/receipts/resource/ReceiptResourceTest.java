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
}
