package dev.shelfspace.receipts.purchase.service;


import dev.shelfspace.receipts.purchase.model.ItemPurchase;
import dev.shelfspace.receipts.purchase.model.ItemStatistics;
import dev.shelfspace.receipts.purchase.model.ReceiptDetail;
import dev.shelfspace.receipts.purchase.model.ReceiptItem;
import dev.shelfspace.receipts.purchase.repository.ReceiptRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Provides item-oriented searches across receipt collection.
 */
@ApplicationScoped
public class ItemService {
    /*
     * Currency averages use two decimal places. HALF_UP makes the rounding rule
     * explicit and produces the behavior normally expected for positive prices:
     * 5.315 becomes 5.32.
     */
    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode CURRENCY_ROUNDING = RoundingMode.HALF_UP;

    private final ReceiptRepository receiptRepository;

    @Inject
    public ItemService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    /**
     * Finds historical purchases whose identifying text contains the supplied
     * query. Results are order newest first for the price-history use cases.
     *
     * @param query case-insensitive item text or sku
     * @return matching purchases, or empty list when nothing matches
     */
    public List<ItemPurchase> findPurchaseHistory(String query){
        String normalizedQuery = query.trim()
                .toLowerCase(Locale.ROOT);

        return receiptRepository.findAll()
                .stream()
                .flatMap(receipt -> receipt.items()
                        .stream()
                        .filter(item -> matches(item, normalizedQuery))
                        .map(item -> createPurchase(receipt, item)))
                .sorted(Comparator.comparing(
                        ItemPurchase::transactionDate
                ).reversed())
                .toList();
    }

    private boolean matches(ReceiptItem item, String normalizedQuery){

        /**
         * Costco exports can use abbreviated item names, more descriptive
         * actual names, or only an SKU. Searching all identified fields gives
         * users a better chance of locating the intended product.
         */
        return containsIgnoreCase(item.itemSku(), normalizedQuery)
                || containsIgnoreCase(item.itemName(), normalizedQuery)
                || containsIgnoreCase(item.itemActualName(), normalizedQuery);

    }

    private boolean containsIgnoreCase(String value, String normalizedQuery){
        if (value == null){
            return false;
        }

        return value.toLowerCase(Locale.ROOT)
                .contains(normalizedQuery);
    }

    private ItemPurchase createPurchase(ReceiptDetail receipt, ReceiptItem item) {
        return new ItemPurchase(
                receipt.orderNumber(),
                receipt.transactionDate(),
                receipt.receiptType(),
                receipt.warehouseInfo(),
                item.itemSku(),
                item.itemName(),
                item.itemActualName(),
                item.description(),
                item.quantity(),
                item.unitPrice(),
                item.lineTotal(),
                item.instantSavings()
        );
    }

    /**
     * Returns purchase history for one exact SKU.
     *
     * <p>The existing free-text search discovers possible matches. This final
     * filter ensures that only purchases whose SKU exactly matches the requested
     * identifier are returned.</p>
     */
    public List<ItemPurchase> findPurchasesBySku(String sku) {
        String normalizedSku = sku.trim();

        return findPurchaseHistory(normalizedSku).stream()
                .filter(purchase ->
                        normalizedSku.equals(purchase.itemSku())
                )
                .toList();
    }

    /**
     * Calculates purchase statistics for one exact SKU.
     *
     * <p>The established exact-SKU method remains the source of purchase history,
     * ensuring this calculation does not read or parse the CSV independently.</p>
     */
    public Optional<ItemStatistics> calculateStatistics(String sku) {
        List<ItemPurchase> purchases = findPurchasesBySku(sku);

        if (purchases.isEmpty()) {
            return Optional.empty();
        }

        // Refunds and non-purchase adjustments do not describe the price paid for
        // a normal purchase, so only positive values participate in price metrics.
        List<ItemPurchase> positivePricePurchases = purchases.stream()
                .filter(purchase -> purchase.unitPrice() != null
                        && purchase.unitPrice().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (positivePricePurchases.isEmpty()) {
            throw new IllegalStateException(
                    "No positive purchase prices are available for SKU '" + sku + "'."
            );
        }

        // Exact-SKU history is newest first, so the first records supply the latest
        // description and latest valid purchase price.
        ItemPurchase latestPurchase = purchases.get(0);
        ItemPurchase latestPositivePricePurchase = positivePricePurchases.get(0);

        long receiptCount = purchases.stream()
                .map(ItemPurchase::orderNumber)
                .distinct()
                .count();

        LocalDate firstPurchaseDate = purchases.stream()
                .map(ItemPurchase::transactionDate)
                .min(LocalDate::compareTo)
                .orElseThrow();

        LocalDate lastPurchaseDate = purchases.stream()
                .map(ItemPurchase::transactionDate)
                .max(LocalDate::compareTo)
                .orElseThrow();

        BigDecimal lowestPrice = positivePricePurchases.stream()
                .map(ItemPurchase::unitPrice)
                .min(BigDecimal::compareTo)
                .orElseThrow();

        BigDecimal highestPrice = positivePricePurchases.stream()
                .map(ItemPurchase::unitPrice)
                .max(BigDecimal::compareTo)
                .orElseThrow();

        BigDecimal averagePrice = calculateAveragePrice(positivePricePurchases);
        List<PurchaseEvent> purchaseEvents =
                findDistinctPurchaseEvents(purchases);
        Long averageDaysBetweenPurchases =
                calculateAverageDaysBetweenPurchases(purchaseEvents);
        long purchaseIntervalCount = Math.max(0, purchaseEvents.size() - 1L);

        return Optional.of(new ItemStatistics(
                latestPurchase.itemSku(),
                selectDisplayName(latestPurchase),
                receiptCount,
                firstPurchaseDate,
                lastPurchaseDate,
                lowestPrice,
                highestPrice,
                averagePrice,
                latestPositivePricePurchase.unitPrice(),
                averageDaysBetweenPurchases,
                purchaseIntervalCount
        ));
    }


    private BigDecimal calculateAveragePrice(List<ItemPurchase> purchases) {
        BigDecimal priceTotal = purchases.stream()
                .map(ItemPurchase::unitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return priceTotal.divide(BigDecimal.valueOf(purchases.size()), CURRENCY_SCALE, CURRENCY_ROUNDING);
    }


    private String selectDisplayName(ItemPurchase purchase) {
        if (purchase.itemActualName() != null && !purchase.itemActualName().isBlank()){
            return purchase.itemActualName();
        }
        if (purchase.itemName() != null && !purchase.itemName().isBlank()){
            return purchase.itemName();
        }
        return purchase.itemSku();
    }

    /**
     * Identifies one purchase occurrence independently of duplicate item rows.
     */
    private record PurchaseEvent(
            String orderNumber,
            LocalDate transactionDate
    ) {
    }

    /**
     * Collapses duplicate SKU rows by receipt identity and returns chronological
     * purchase events for interval calculations.
     */
    private List<PurchaseEvent> findDistinctPurchaseEvents(
            List<ItemPurchase> purchases
    ) {
        return purchases.stream()
                .collect(Collectors.toMap(
                        ItemPurchase::orderNumber,
                        purchase -> new PurchaseEvent(
                                purchase.orderNumber(),
                                purchase.transactionDate()
                        ),
                        // Repeated item rows from one receipt represent one event.
                        (first, duplicate) -> first
                ))
                .values().stream()
                .sorted(Comparator.comparing(PurchaseEvent::transactionDate)
                        .thenComparing(PurchaseEvent::orderNumber))
                .toList();
    }

     /**
     * Calculates the typical whole-day interval between chronological purchases.
     * A single purchase has no interval, so the average is intentionally absent.
     */
    private Long calculateAverageDaysBetweenPurchases(
            List<PurchaseEvent> events
    ) {

        if (events.size() < 2) {
            return null;
        }

        long totalDaysBetweenPurchases = 0;

        for (int index = 1; index < events.size(); index++) {
            PurchaseEvent previous = events.get(index - 1);
            PurchaseEvent current = events.get(index);

            totalDaysBetweenPurchases += ChronoUnit.DAYS.between(
                    previous.transactionDate(),
                    current.transactionDate()
            );
        }

        int intervalCount = events.size() - 1;

        return BigDecimal.valueOf(totalDaysBetweenPurchases)
                .divide(
                        BigDecimal.valueOf(intervalCount),
                        0,
                        RoundingMode.HALF_UP
                )
                .longValueExact();
    }
}
