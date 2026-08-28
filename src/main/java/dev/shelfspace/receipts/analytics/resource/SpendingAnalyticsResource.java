package dev.shelfspace.receipts.analytics.resource;

import dev.shelfspace.receipts.analytics.model.MonthlySpending;
import dev.shelfspace.receipts.analytics.service.SpendingAnalyticsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
    public List<MonthlySpending> getMonthlySpending(){
        return spendingAnalyticsService.getMonthlySpending();
    }
}
