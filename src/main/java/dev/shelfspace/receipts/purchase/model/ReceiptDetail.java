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
        BigDecimal discountAmount,
        BigDecimal shopCardApplied,
        BigDecimal couponApplied,
        BigDecimal taxTotal,
        BigDecimal shippingHandling,
        BigDecimal deliveryFees,
        BigDecimal finalTotal,
        List<ReceiptItem> items
) {
}
