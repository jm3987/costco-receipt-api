package dev.shelfspace.receipts.purchase.resource;


import dev.shelfspace.receipts.purchase.model.ReceiptDetail;
import dev.shelfspace.receipts.purchase.service.ReceiptService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/receipts")
@Produces(MediaType.APPLICATION_JSON)
public class ReceiptResource {
    private final ReceiptService receiptService;

    @Inject
    public ReceiptResource(ReceiptService receiptService){
        this.receiptService = receiptService;
    }

    @GET
    @Path("/{orderNumber}")
    public ReceiptDetail getReceipt(
            @PathParam("orderNumber") String orderNumber
    ){
        return receiptService.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException(
                        "Receipt not found: " + orderNumber
                ));
    }

}
