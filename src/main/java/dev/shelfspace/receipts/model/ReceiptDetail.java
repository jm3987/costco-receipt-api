package dev.shelfspace.receipts.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReceiptDetail(
        String orderNumber,
        String receiptId,
        String receiptType,
        LocalDate transactionDate,
        String warehouseInfo,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal finalTotal,
        List<ReceiptItem> items
) {
}
