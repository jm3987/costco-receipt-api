package dev.shelfspace.receipts.analytics.resource;

import dev.shelfspace.receipts.analytics.model.MonthlySpending;
import dev.shelfspace.receipts.analytics.model.SpendingAnalyticsQuery;
import dev.shelfspace.receipts.analytics.service.SpendingAnalyticsService;
import dev.shelfspace.receipts.common.ApiError;
import dev.shelfspace.receipts.common.web.QueryParameterParser;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/analytics/spending")
@Produces(MediaType.APPLICATION_JSON)
public class SpendingAnalyticsResource {
    private final SpendingAnalyticsService spendingAnalyticsService;

    @Inject
    public SpendingAnalyticsResource(SpendingAnalyticsService spendingAnalyticsService) {
        this.spendingAnalyticsService = spendingAnalyticsService;
    }

    @GET
    @Path("/monthly")
    public List<MonthlySpending> getMonthlySpending(
            @QueryParam("from") String from,
            @QueryParam("to") String to
    ) {
        try {
            SpendingAnalyticsQuery query = new SpendingAnalyticsQuery(
                    QueryParameterParser.parseOptionalDate(from, "from"),
                    QueryParameterParser.parseOptionalDate(to, "to")
            );

            return spendingAnalyticsService.findMonthlySpending(query);
        } catch (IllegalArgumentException exception) {
            throw badRequest(
                    "INVALID_SPENDING_ANALYTICS_QUERY",
                    exception.getMessage(),
                    exception
            );
        }
    }

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
