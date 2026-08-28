package dev.shelfspace.receipts.purchase.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class ItemResourceTest {

    @Test
    void findsPurchaseByItemNameIgnoringCase(){
        given()
                .queryParam("query", "milk")
                .when()
                .get("/api/items")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].orderNumber", is("ORDER-100"))
                .body("[0].orderNumber", is("ORDER-100"))
                .body("[0].transactionDate", is("2025-06-15"))
                .body("[0].itemSku", is("SKU-001"))
                .body("[0].itemName", is("MILK"))
                .body("[0].unitPrice", is(4.99F));
    }

    @Test
    void findsPurchaseByActualItemName(){
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
    void returnEmptyListWhenNothingMatches(){
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


}
