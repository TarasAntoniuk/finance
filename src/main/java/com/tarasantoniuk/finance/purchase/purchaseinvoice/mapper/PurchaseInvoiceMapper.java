package com.tarasantoniuk.finance.purchase.purchaseinvoice.mapper;

import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceLineResponse;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceResponse;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceSummary;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.entity.PurchaseInvoice;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.entity.PurchaseInvoiceLine;
import org.springframework.stereotype.Component;

@Component
public class PurchaseInvoiceMapper {

    public PurchaseInvoiceResponse toResponse(PurchaseInvoice invoice) {
        return new PurchaseInvoiceResponse(
                invoice.getId(),
                invoice.getOrganization().getId(),
                invoice.getSupplier().getId(),
                invoice.getSupplier().getName(),
                invoice.getSupplierInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getTransactionDateTime(),
                invoice.getCurrency().getCode(),
                invoice.getBuyerReference(),
                invoice.getDescription(),
                invoice.getStatus(),
                invoice.getPostedAt(),
                invoice.getTotalNet(),
                invoice.getTotalVat(),
                invoice.getTotalGross(),
                invoice.vatBreakdown(),
                invoice.getLines().stream().map(this::toLineResponse).toList());
    }

    public PurchaseInvoiceSummary toSummary(PurchaseInvoice invoice) {
        return new PurchaseInvoiceSummary(
                invoice.getId(),
                invoice.getOrganization().getId(),
                invoice.getSupplier().getId(),
                invoice.getSupplier().getName(),
                invoice.getSupplierInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getCurrency().getCode(),
                invoice.getStatus(),
                invoice.getTotalGross());
    }

    private PurchaseInvoiceLineResponse toLineResponse(PurchaseInvoiceLine line) {
        return new PurchaseInvoiceLineResponse(
                line.getLineNumber(),
                line.getItem().getId(),
                line.getItem().getCode(),
                line.getItem().getName(),
                line.getDescription(),
                line.getQuantity(),
                line.getUnitCode(),
                line.getUnitPriceNet(),
                line.getLineNet(),
                line.getVatCategory(),
                line.getVatRate());
    }
}
