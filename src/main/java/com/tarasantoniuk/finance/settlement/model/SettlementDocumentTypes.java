package com.tarasantoniuk.finance.settlement.model;

/**
 * Names of the document types that take part in counterparty settlements. Owned by the settlement
 * module, so the modules that write settlements share one spelling without depending on each other.
 */
public final class SettlementDocumentTypes {

    public static final String PURCHASE_INVOICE = "PurchaseInvoice";
    public static final String BANK_PAYMENT = "BankPayment";

    private SettlementDocumentTypes() {
    }
}
