package com.tarasantoniuk.finance.purchase.purchaseinvoice.dto;

import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.entity.VatBreakdown;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseInvoiceResponse(
        Long id,
        Long organizationId,
        Long supplierId,
        String supplierName,
        String supplierInvoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDateTime postingDateTime,
        String currencyCode,
        String buyerReference,
        String note,
        DocumentStatus status,
        LocalDateTime postedAt,
        BigDecimal totalNet,
        BigDecimal totalVat,
        BigDecimal totalGross,
        List<VatBreakdown> vatBreakdown,
        List<PurchaseInvoiceLineResponse> lines
) {
}
