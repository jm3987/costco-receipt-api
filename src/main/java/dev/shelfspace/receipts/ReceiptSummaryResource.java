package dev.shelfspace.receipts;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDate;


@Path("/api/receipts/summary")
@Produces(MediaType.APPLICATION_JSON)
public class ReceiptSummaryResource {

    @GET
    public ReceiptSummary getSummary(){
        return new ReceiptSummary(
                1744,
                213,
                LocalDate.of(2024,1, 11),
                LocalDate.of(2026, 7, 13)
        );
    }
}
