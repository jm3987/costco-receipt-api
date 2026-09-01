package dev.shelfspace.receipts.purchase.model;


import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Defines the supported ordering strategies for receipt-list results.
 *
 * the external values match the format accepted by the sort query parameter.
 */
public enum ReceiptSort {
    DATE_DESC("date,desc"),
    DATE_ASC("date,asc");
    private final String value;
    ReceiptSort(String value) {
        this.value = value;
    }

    /**
     * Returns the value accepted by the HTTP API.
     */
    public String value(){
        return value;
    }

    /**
     * Converts an external query value into a supported ordering strategy.
     *
     * @throws IllegalArgumentException when the request sort is unsupported
     */
    public static ReceiptSort fromValue(String value) {
        String normalizedValue = value.trim();
        return Arrays.stream(values())
                .filter(sort -> sort.value.equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported receipt sort '" + value
                                + "'. Supported values: "
                                + supportedValues() + "."
                ));
    }

    /**
     * Produces the supported values used in validation messages.
     */
    public static String supportedValues() {
        return Arrays.stream(values())
                .map(ReceiptSort::value)
                .collect(Collectors.joining(", "));
    }
}
