package dev.shelfspace.receipts.analytics.model;

import java.time.LocalDate;

public record SpendingAnalyticsQuery(
        LocalDate from,
        LocalDate to
) {
    public SpendingAnalyticsQuery {
        // Inclusive ranges must proceed forward in time.
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Query parameter 'from' must not be after 'to'."
            );
        }
    }

    public boolean includes(LocalDate date) {
        boolean onOrAfterFrom = from == null || !date.isBefore(from);
        boolean onOrBeforeTo = to == null || !date.isAfter(to);
        return onOrAfterFrom && onOrBeforeTo;
    }
}
