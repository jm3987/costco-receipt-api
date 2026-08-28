package dev.shelfspace.receipts.analytics.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class SpendingAnalyticsResourceTest {

    @Test
    void returnsMonthlySpendingInChronologicalOrder() {
        given()
                .when()
                .get("/api/analytics/spending/monthly")
                .then()
                .statusCode(200)
                .body("size()", is(3))

                //Verify ordering explicitly because clients sound not
                //need to sort the data before displaying a chart
                .body("[0].month", is("2024-01"))
                .body("[0].receiptCount", is(1))
                .body("[0].totalSpending", is(15.00F))

                .body("[1].month", is("2025-06"))
                .body("[1].receiptCount", is(1))
                .body("[1].totalSpending", is(8.64F))

                .body("[2].month", is("2026-02"))
                .body("[2].receiptCount", is(1))
                .body("[2].totalSpending", is(16.24F));
    }
}
