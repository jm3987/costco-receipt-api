package dev.shelfspace.receipts.purchase.resource;


import dev.shelfspace.receipts.purchase.service.ReceiptSummaryService;
import dev.shelfspace.receipts.purchase.model.ReceiptSummary;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


@Path("/api/receipts/summary")
@Produces(MediaType.APPLICATION_JSON)
public class ReceiptSummaryResource {

    private final ReceiptSummaryService receiptSummaryService;

    @Inject
    public ReceiptSummaryResource(ReceiptSummaryService receiptSummaryService){
        this.receiptSummaryService = receiptSummaryService;
    }

    @GET
    public ReceiptSummary getSummary(){
        return receiptSummaryService.getSummary();
    }
}
