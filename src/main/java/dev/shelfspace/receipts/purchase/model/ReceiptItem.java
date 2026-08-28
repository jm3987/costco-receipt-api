package dev.shelfspace.receipts.purchase.model;

import java.math.BigDecimal;

public record ReceiptItem(
        String itemSku,
        String itemName,
        String itemActualName,
        String description,
        String itemWeight,
        String imageUrl,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String departmentId,
        boolean taxable,
        Boolean fsaEligible,
        BigDecimal instantSavings,
        BigDecimal surcharge,
        String surchargeReason
) {
}
