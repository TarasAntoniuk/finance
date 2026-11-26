package com.tarasantoniuk.finance.purchase.purchaseinvoice.dto;

import com.tarasantoniuk.finance.core.item.enums.VatCategory;

import java.math.BigDecimal;

public record PurchaseInvoiceLineResponse(
        int lineNumber,
        Long itemId,
        String itemCode,
        String itemName,
        String description,
        BigDecimal quantity,
        String unitCode,
        BigDecimal unitPriceNet,
        BigDecimal lineNet,
        VatCategory vatCategory,
        BigDecimal vatRate
) {
}
