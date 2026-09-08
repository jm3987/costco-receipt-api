package dev.shelfspace.receipts.common.web;

import dev.shelfspace.receipts.common.ApiError;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Converts shared HTTP query-parameter representations into application types.
 */
public final class QueryParameterParser {
    private static final String INVALID_DATE_PARAMETER = "INVALID_DATE_PARAMETER";

    private QueryParameterParser() {
    }

    /**
     * Converts an optional ISO-8601 query parameter into a {@link LocalDate}.
     *
     * @param value raw query parameter value, or {@code null} when omitted
     * @param parameterName parameter name used in validation messages
     * @return the parsed date, or {@code null} for an omitted or blank parameter
     */
    public static LocalDate parseOptionalDate(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            String message = "Query parameter '" + parameterName
                    + "' must use the YYYY-MM-DD format.";
            ApiError error = new ApiError(INVALID_DATE_PARAMETER, message);

            Response response = Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error)
                    .build();

            throw new BadRequestException(response, exception);
        }
    }
}
