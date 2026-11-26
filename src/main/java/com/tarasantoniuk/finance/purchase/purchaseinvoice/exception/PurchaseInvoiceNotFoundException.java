package com.tarasantoniuk.finance.purchase.purchaseinvoice.exception;

import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;

public class PurchaseInvoiceNotFoundException extends ResourceNotFoundException {

    public PurchaseInvoiceNotFoundException(Long id) {
        super("Purchase invoice", "id", id);
    }
}
