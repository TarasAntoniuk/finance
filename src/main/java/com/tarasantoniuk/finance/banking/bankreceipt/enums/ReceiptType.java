package com.tarasantoniuk.finance.banking.bankreceipt.enums;

/**
 * Types of bank receipt transactions
 */
public enum ReceiptType {
    /**
     * Payment received from customer for goods/services
     */
    CUSTOMER_PAYMENT,

    /**
     * Loan received from bank or financial institution
     */
    LOAN_RECEIVED,

    /**
     * Investment or capital contribution
     */
    INVESTMENT,

    /**
     * Refund from supplier or other party
     */
    REFUND,

    /**
     * Interest income from deposits
     */
    INTEREST_INCOME,

    /**
     * Internal transfer from another account
     */
    INTERNAL_TRANSFER,

    /**
     * Other income not covered by above types
     */
    OTHER_INCOME
}