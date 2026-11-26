package com.tarasantoniuk.finance.purchase.purchaseinvoice.exception;

import com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException;

/**
 * The same supplier invoice number is already registered for this organization and supplier.
 */
public class DuplicateSupplierInvoiceException extends ResourceAlreadyExistsException {

    public DuplicateSupplierInvoiceException(String supplierInvoiceNumber) {
        super("Purchase invoice", "supplierInvoiceNumber", supplierInvoiceNumber);
    }
}
