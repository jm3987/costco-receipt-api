package dev.shelfspace.receipts.purchase.resource;


import dev.shelfspace.receipts.purchase.model.ReceiptDetail;
import dev.shelfspace.receipts.purchase.model.ReceiptPage;
import dev.shelfspace.receipts.purchase.model.ReceiptQuery;
import dev.shelfspace.receipts.purchase.service.ReceiptService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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
//
//    @GET
//    public ReceiptPage listReceipts(
//            @QueryParam("page") @DefaultValue("0") int page,
//            @QueryParam("size") @DefaultValue("20") int size
//    ){
//        return receiptService.listReceipts(page, size);
//    }

    @GET
    public ReceiptPage listReceipts(
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size
    ) {
        ReceiptQuery query = new ReceiptQuery(
                parseDate(from, "from"),
                parseDate(to, "to"),
                page,
                size
        );

        try {
            return receiptService.listReceipts(query);
        } catch (IllegalArgumentException exception) {
            // Translate invalid business input into the HTTP contract exposed
            // by this resource. The service remains independent of Jakarta REST.
            throw new BadRequestException(exception.getMessage(), exception);
        }
    }

    /**
     * Converts an optional ISO-8601 query parameter into a LocalDate.
     *
     * @param value raw query parameter value, or null when it was not supplied
     * @param parameterName parameter name used in validation messages
     * @return the parsed date, or null for an omitted parameter
     */
    private LocalDate parseDate(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            // Missing dates represent open-ended ranges rather than errors.
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new BadRequestException(
                    "Query parameter '" + parameterName
                            + "' must use the YYYY-MM-DD format.",
                    exception
            );
        }
    }




}
