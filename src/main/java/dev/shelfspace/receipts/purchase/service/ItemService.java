package dev.shelfspace.receipts.purchase.service;


import dev.shelfspace.receipts.purchase.model.ItemPurchase;
import dev.shelfspace.receipts.purchase.model.ReceiptDetail;
import dev.shelfspace.receipts.purchase.model.ReceiptItem;
import dev.shelfspace.receipts.purchase.repository.ReceiptRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;


/**
 * Provides item-oriented searches across receipt collection.
 */
@ApplicationScoped
public class ItemService {
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

}
