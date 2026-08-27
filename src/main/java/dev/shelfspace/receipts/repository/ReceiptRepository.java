package dev.shelfspace.receipts.repository;

import dev.shelfspace.receipts.model.ReceiptDetail;

import java.util.List;
import java.util.Optional;

/**
 * Provides storage-independent access to receipt data.
 *
 * Application services depend on this contract rather than a particular
 * storage format, allowing the CSV implementation to be replaced later
 * without changing the service or resource layers.
 */
public interface ReceiptRepository {

    /**
     * Finds a receipt using the order number shared by warehouse,
     * gas-station, and online receipt exports.
     *
     * @param orderNumber the external receipt order number
     * @return the matching receipt, or an empty result when none exists
     */
    Optional<ReceiptDetail> findByOrderNumber(String orderNumber);
}
