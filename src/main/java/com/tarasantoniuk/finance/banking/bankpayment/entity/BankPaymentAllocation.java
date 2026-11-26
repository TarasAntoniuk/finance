package com.tarasantoniuk.finance.banking.bankpayment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/**
 * Part of a supplier payment explicitly assigned to one purchase invoice.
 */
@Embeddable
public class BankPaymentAllocation {

    @Column(name = "purchase_invoice_id", nullable = false)
    private Long purchaseInvoiceId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    protected BankPaymentAllocation() {
    }

    public BankPaymentAllocation(Long purchaseInvoiceId, BigDecimal amount) {
        this.purchaseInvoiceId = purchaseInvoiceId;
        this.amount = amount;
    }

    public Long getPurchaseInvoiceId() {
        return purchaseInvoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
