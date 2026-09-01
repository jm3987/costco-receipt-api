package dev.shelfspace.receipts.purchase.model;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Identifies the sales channel represented by an imported receipt.
 *
 * <p>The external values intentionally match the values stored in the
 * receipt CSV and accepted by the receipt-list API.</p>
 */
public enum PurchaseType {

    WAREHOUSE("warehouse"),
    GAS_STATION("gas_station"),
    ONLINE("online");

    private final String value;

    PurchaseType(String value) {
        this.value = value;
    }

    /**
     * Returns the canonical value used by the CSV and HTTP API.
     */
    public String value() {
        return value;
    }

    /**
     * Converts a case-insensitive external value into a supported type.
     *
     * @throws IllegalArgumentException when the supplied value is unsupported
     */
    public static PurchaseType fromValue(String value) {
        String normalizedValue = value.trim();

        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported purchase type '" + value
                                + "'. Supported values: "
                                + supportedValues() + "."
                ));
    }

    /**
     * Produces a user-facing list for validation error messages.
     */
    public static String supportedValues() {
        return Arrays.stream(values())
                .map(PurchaseType::value)
                .collect(Collectors.joining(", "));
    }
}