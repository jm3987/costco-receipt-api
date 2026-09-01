package dev.shelfspace.receipts.common;


/**
 * Represents a stable JSON error response returned by the API.
 *
 * @param code machine-readable identifier suitable for future UI handling
 * @param message human-readable explanation of the invalid request
 */
public record ApiError(
        String code,
        String message
) {
}
