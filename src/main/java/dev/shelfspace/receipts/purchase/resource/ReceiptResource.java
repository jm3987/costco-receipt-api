package dev.shelfspace.receipts.purchase.resource;


import dev.shelfspace.receipts.common.ApiError;
import dev.shelfspace.receipts.purchase.model.*;
import dev.shelfspace.receipts.purchase.service.ReceiptService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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


    @GET
    public ReceiptPage listReceipts(
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("type") String type,
            @QueryParam("sort") @DefaultValue("date,desc") String sort,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size
    ) {
        ReceiptQuery query = new ReceiptQuery(
                parseDate(from, "from"),
                parseDate(to, "to"),
                parsePurchaseType(type),
                parseReceiptSort(sort),
                page,
                size
        );

        try {
            return receiptService.listReceipts(query);
        } catch (IllegalArgumentException exception) {
            throw badRequest(
                    "INVALID_RECEIPT_QUERY",
                    exception.getMessage(),
                    exception
            );
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

    /**
     * Converts an optional query parameter into a supported purchase type.
     *
     * @param value raw value supplied through the type query parameter
     * @return the matching purchase type, or null when no filter was supplied
     */
    private PurchaseType parsePurchaseType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return PurchaseType.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw badRequest(
                    "INVALID_PURCHASE_TYPE",
                    exception.getMessage(),
                    exception
            );
        }
    }

    /**
     * Converts the optional HTTP sort parameter into a supported receipt-ordering
     * strategy.
     */
    private ReceiptSort parseReceiptSort(String value) {
        if (value == null || value.isBlank()) {
            return ReceiptSort.DATE_DESC;
        }

        try {
            return ReceiptSort.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw badRequest(
                    "INVALID_RECEIPT_SORT",
                    exception.getMessage(),
                    exception
            );
        }
    }

    /**
     * Creates the standard JSON response used for invalid receipt-list requests.
     */
    private BadRequestException badRequest(
            String code,
            String message,
            Throwable cause
    ) {
        ApiError error = new ApiError(code, message);

        Response response = Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();

        return new BadRequestException(response, cause);
    }



}
