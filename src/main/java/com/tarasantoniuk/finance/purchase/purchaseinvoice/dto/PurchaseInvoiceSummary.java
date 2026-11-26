package com.tarasantoniuk.finance.purchase.purchaseinvoice.dto;

import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseInvoiceSummary(
        Long id,
        Long organizationId,
        Long supplierId,
        String supplierName,
        String supplierInvoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        String currencyCode,
        DocumentStatus status,
        BigDecimal totalGross
) {
}
